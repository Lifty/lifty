package org.lifty.engine.io

import scalaz._
import scalaz.effects._
import Scalaz._
import java.io._

object FileUtil {

  def file(path: String) = new File(path)
  
  /** 
   * Reads the contents of a file to a string.  
   * 
   * @param file The file to read
   * @return A String containing the contents of the File.
   */
  def readToString(file: File): IO[Option[String]] = io { 
    
    try { 
      val in   = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
      var line = in.readLine()
      var text = new StringBuffer("")
      var first = true
      while (line != null) {
        if (first) {
          text.append(line)
          first = false
        } else {
          text.append("\n"+line)
        }
        line = in.readLine()
      }
      Some(text.toString)
    } catch {
      case e: Exception => 
        e.printStackTrace()
        None
    }
  }
  
  /** 
   * Writes the contents string to a temporary File and returns a handle to that File 
   * 
   * @param contents  The contents to write to the file
   * @return          The newly created temporary File
   */
  def writeToTempFile(contents: String): Option[File] = { 
    try {      
      val file = File.createTempFile("lifty",".ssp")
      file.deleteOnExit() // delete when the VM exists. 
      writeToFile(contents,file, check = false)
    } catch {
      case e: Exception => 
        e.printStackTrace()
        None
    }
  }
  
  /** 
   * Writes the contents string to a File and returns a handle to that File 
   * 
   * @param contents  The contents to write to the file
   * @param file      The File to write to
   * @return          The File that it wrote the contents to.
   */
  def writeToFile(contents: String, file: File, check: Boolean = true): Option[File] = {
    
    import org.lifty.engine.Functional._
    
    def create() = {
      if (file.isDirectory) {
        file.mkdirs()
      } else {
        
        val parentPath = File.separator match {
		case "\\" => (file.getAbsolutePath.split("\\\\").init.mkString("\\\\")) // for windows
		case _ => (file.getAbsolutePath.split(File.separator).init.mkString(File.separator)) // for linux and others
	      		
      	}
        new File(parentPath).mkdirs()
        file.createNewFile()
      }
      val out = new BufferedWriter(new FileWriter(file));
      out.write(contents);
      out.close();
      Some(file)
    }
    
    try {
      if (check) {
        if (safeToCreateFile(file)) create() else None 
      } else create()
    } catch {
      case e: Exception => 
        e.printStackTrace()
        None
    }
  }
  
  private def safeToCreateFile(file: File): Boolean = {
    
    def askUser: Boolean = {
      val question = "The file %s exists, do you want to override it? (y/n): ".format(file.getPath)
      InputReaderComponent.requestInput(question).unsafePerformIO match {
        case "y" => true
        case "n" => false
        case _ => askUser
      }
    }
    
    if (file.exists) askUser else true
  }
  
}
