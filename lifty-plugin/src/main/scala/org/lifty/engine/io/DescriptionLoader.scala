package org.lifty.engine.io

import org.lifty.engine.{ Description, Error }
import scalaz._
import scalaz.effects._
import Scalaz._
import java.io.{ FileInputStream, InputStreamReader, File}
import java.net.{ URL }
import net.liftweb.json.{ JsonParser, DefaultFormats }

/** 
 * Contains method(s) for loading and parsing the description.json files of 
 * recipes. 
 */
object DescriptionLoader {

  // Simply so it knows how to println this stuff for debugging.
  implicit val formats = DefaultFormats

  def load(description: URL): IO[Validation[Error, Description]] = io {
    val file = File.createTempFile("descriptor",".json")
    file.deleteOnExit() 
    Downloader.download(description, file).unsafePerformIO.fold(
      (e) => e.fail,
      (s) => load(file).unsafePerformIO.fold(
        (e) => e.fail,
        (s) => s.success
      )
    )
  }

  // Load a json description of a recipe 
  def load(description: File): IO[Validation[Error, Description]] = io {
    attemptToGetStream(description).fold(
      err         => err.fail,
      inputStream => (for {
        jvalue      <- JsonParser.parseOpt(inputStream)
        description <- jvalue.extractOpt[Description]
      } yield description.success).getOrElse(Error("Wasn't able to parse json file: " + description).fail)
    )
  }

  // This will attempt to create an InputStreamReader from the url passed.
  private def attemptToGetStream(file: File): Validation[Error, InputStreamReader] = {
    try {
      new InputStreamReader(new FileInputStream(file)).success
    } catch {
      case e: Exception => Error("Wasn't able to read the file: %s ".format(e.getMessage)).fail
    }
  }
}