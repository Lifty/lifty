/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate


import scaml.ScamlCodeGenerator
import java.net.URLClassLoader
import scala.collection.mutable.HashMap
import scala.compat.Platform
import ssp.{SspCodeGenerator, ScalaCompiler}
import util.IOUtil
import java.io.{File}


class TemplateEngine {
  private class CacheEntry(val template: Template, val timestamp: Long, val dependencies: Set[String])

  var pageFileEncoding = "UTF-8"
  var allowReload = true

  var resourceLoader: ResourceLoader = new FileResourceLoader
  var codeGenerators: Map[String, CodeGenerator] = Map("ssp" -> new SspCodeGenerator, "scaml" -> new ScamlCodeGenerator)

  lazy val compiler = new ScalaCompiler(bytecodeDirectory, classpath)

  def sourceDirectory = new File(workingDirectoryRoot, "source")
  def bytecodeDirectory = new File(workingDirectoryRoot, "bytecode")

  var classpath: String = null
  var workingDirectoryRoot: File = null

  private val templateCache = new HashMap[(String, List[Binding]), CacheEntry]

  def load(uri: String, bindings: Binding*) = {

    val bindingsList = bindings.toList;
    val key = (uri, bindings.toList)
    val timestamp = Platform.currentTime

    // TODO: mangle the params and include in the workingDirectory path
    val workingDirectory = new File(workingDirectoryRoot, uri)
    val bytecodeDirectory = new File(workingDirectory, "bytecode")

    // Obtain the template object that should service this request (creating it on the fly if needed)
    templateCache.synchronized {
      // Determine whether to build/rebuild the template, load existing .class files from the file system,
      // or reuse an existing template that we've already loaded
      val cacheEntry = templateCache.get(key)
      ((cacheEntry match {
        case None =>
          val ch = listFiles(bytecodeDirectory)
          if (bytecodeDirectory.exists && ch.length > 0 && findNewestTimestamp(ch, bytecodeDirectory.lastModified) >= lastModified(uri))
            'LoadPrebuilt
          else
            'Build
        case Some(entry) =>
          if (allowReload && entry.dependencies.exists {lastModified(_) > entry.timestamp})
            'Build
          else
            'AlreadyLoaded
      }) match {
        case 'AlreadyLoaded =>
          cacheEntry.get
        case 'Build =>
          val newCacheEntry = preparePage(timestamp, uri, bindingsList)
          templateCache += (key -> newCacheEntry)
          newCacheEntry
        case 'LoadPrebuilt =>
          val className = codeGenerator(uri).className(uri, bindingsList)
          val template = createTemplate(className, bytecodeDirectory)
          val dependencies = Set.empty[String] + uri
          val newCacheEntry = new CacheEntry(template, timestamp, dependencies)
          templateCache += (key -> newCacheEntry)
          newCacheEntry
      }).template
    }
  }

  /**
   * Does no caching; useful for temporary templates or temapltes created dynamically such as from the contents of a message
   */
  def loadTemporary(uri: String, bindings: Binding*) = {

    val argsList = bindings.toList;
    val timestamp = Platform.currentTime

    val newCacheEntry = preparePage(timestamp, uri, argsList)
    newCacheEntry.template
  }

  def codeGenerator(uri: String): CodeGenerator = {
    val t = uri.split("\\.")
    if (t.length < 2) {
      throw new TemplateException("Template file extension missing.  Cannot determine which template processor to use.");
    } else {
      val extension = t.last
      codeGeneratorForExtension(extension)
    }
  }

  def codeGeneratorForExtension(extension: String): CodeGenerator = {
    val rc = codeGenerators.get(extension).get
    if (rc == null) {
      throw new TemplateException("Not a template file extension (" + codeGenerators.keys.mkString("|") + "), you requested: " + extension);
    }
    rc;
  }

  private def listFiles(bytecodeDirectory: File) = bytecodeDirectory.listFiles match {
    case null => Array[File]()
    case x: Array[File] => x
  }

  private def findNewestTimestamp(path: File): Long = {
    val c = listFiles(path)
    findNewestTimestamp(c, path.lastModified)
  }

  private def findNewestTimestamp(children: Array[File], defaultValue: Long): Long = {
    if (children.length == 0)
      defaultValue
    else
      children.foldLeft(defaultValue)((newestSoFar, file) => {
        val newestInSubtree = findNewestTimestamp(file)
        if (newestSoFar > newestInSubtree) newestSoFar else newestInSubtree
      })
  }


  private def lastModified(uri: String): Long =
    resourceLoader.lastModified(uri)


  private def preparePage(timestamp: Long, uri: String, bindings: List[Binding]): CacheEntry = {
    try {
      generate_compile_and_load(timestamp, uri, bindings)
    } catch {
      case e:InstantiationException=>{
        try {
          println("First try resulted in a InstantiationException.. let try one more time..");
          generate_compile_and_load(timestamp, uri, bindings)
        } catch {
          case e:Throwable=>{
            e.printStackTrace
            val cause = e.getCause
            if (cause != null && cause != e) {
              print("Caused by: ")
              cause.printStackTrace
            }
            throw new TemplateException("Could not load template: "+e, e);
          }
        }
      }
      case e:Throwable=>{
        e.printStackTrace
        val cause = e.getCause
        if (cause != null && cause != e) {
          print("Caused by: ")
          cause.printStackTrace
        }
        throw new TemplateException("Could not load template: "+e, e);
      }
    }
  }

  private def generate_compile_and_load(timestamp: Long, uri: String, bindings: List[Binding]): CacheEntry = {

    // Generate the scala source code from the template
    val code = codeGenerator(uri).generate(this, uri, bindings)

    // Write the source code to file..
    val sourceFile = new File(sourceDirectory, uri+".scala")
    sourceFile.getParentFile.mkdirs
    IOUtil.writeBinaryFile(sourceFile, code.source.getBytes("UTF-8"))

    // Compile the generated scala code
    compiler.compile(sourceFile)

    // Load the compiled class and instantiate the template object
    val template = createTemplate(code.className, bytecodeDirectory)

    new CacheEntry(template, timestamp, code.dependencies)
  }



  private def createTemplate(className: String, bytecodeDirectory: File): Template = {
    // Load the compiled class
    val classLoader = new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), this.getClass.getClassLoader)
    val clazz = classLoader.loadClass(className)
    clazz.asInstanceOf[Class[Template]].newInstance
  }

}

