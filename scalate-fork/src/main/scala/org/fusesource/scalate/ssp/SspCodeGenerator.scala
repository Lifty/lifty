/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2010, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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

package org.fusesource.scalate.ssp

import org.fusesource.scalate._
import java.net.URI
import scala.util.matching.Regex


class SspCodeGenerator  extends AbstractCodeGenerator[PageFragment] {

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {

    def generate(fragments: List[PageFragment]):Unit = {
      fragments.foreach{ generate(_) }
    }

    def generate(fragment: PageFragment):Unit = {
      fragment match {
        case CommentFragment(code) => {
        }
        case ScriptletFragment(code) => {
          this << code
        }
        case TextFragment(text) => {
          this << "$_scalate_$_context << ( " + asString(text) + " );"
        }
        case af:AttributeFragment => {
        }
        case DollarExpressionFragment(code) => {
          this << "$_scalate_$_context <<< "+code+""
        }
        case ExpressionFragment(code) => {
          this << "$_scalate_$_context << "+code+""
        }
      }
    }

  }

  var useTemplateNameToDiscoverModel = true
  var translationUnitLoader = new SspLoader
  
  override def generate(engine:TemplateEngine, uri:String, bindings:List[Binding]): Code = {

    // Load the translation unit
    val tu = translationUnitLoader.loadTranslationUnit(engine, uri)
    val translationUnit = tu.content

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = (new SspParser).getPageFragments(translationUnit)

    // Convert the parsed representation to Scala source code
    val params = bindings ::: findParams(uri, fragments)

    val sb = new SourceBuilder
    sb.generate(packageName, className, params, fragments)

    Code(this.className(uri, bindings), sb.code, tu.dependencies)
  }

  private val classNameInUriRegex = """(\w+([\\|\.]\w+)*)\.\w+\.\w+""".r

  private def findParams(uri: String, fragments: List[PageFragment]): List[Binding] = {

    val answer = fragments.flatMap {
      case p: AttributeFragment => List(Binding(p.name, p.className, p.autoImport, p.defaultValue, p.kind))
      case _ => Nil
    }
    
    if (useTemplateNameToDiscoverModel && answer.isEmpty) {
      // TODO need access to the classloader!!

      classNameInUriRegex.findFirstMatchIn(uri) match {
        case Some(m: Regex.Match) => val cn = m.group(1)
        Nil
        case _ => Nil
      }
    } else {
      answer
    }
  }

}

