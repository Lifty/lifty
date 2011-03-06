package org.fusesource.scalate

/**
 * Created by IntelliJ IDEA.
 * User: chirino
 * Date: Feb 1, 2010
 * Time: 10:24:35 AM
 * To change this template use File | Settings | File Templates.
 */

trait RenderContext {

  /**
   * Allow the right hand side to be written to the stream which makes it easy to code
   * generate expressions using blocks in the SSP code
   */
  def <<(value: Any): Unit

  /**
   * Like << but sanitizes / XML escapes the right hand side
   */
  def <<<(value: Any): Unit;

  /**
   * Gets the value of a template variable binding
   */
  def binding(name:String): Option[Any];

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def capture(body: => Unit): String;

  implicit def body(body: => Unit): () => String = {
    () => {
      capture(body)
    }
  }

}