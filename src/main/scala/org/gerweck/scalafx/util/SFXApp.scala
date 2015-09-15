package org.gerweck.scalafx.util

import scala.util.control.NonFatal

import scalafx.application.JFXApp

import org.log4s.getLogger


/** A ScalaFX application that exposes the parameters and state a bit better
  * than how the ScalaFX standard [[scalafx.application.JFXApp]] does it.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
trait SFXApp extends JFXApp {
  /* TBD: Get the ScalaFX team to expose the app args. Their config system is
   * not only silly, but they seem to think they should make it impossible
   * to use anything else.
   */
  protected def args = _args
  private[this] var _args: Array[String] = _

  override def main(args: Array[String]) = {
    try {
      _args = args
      super.main(args)
    } catch {
      case NonFatal(e) =>
        getLogger.error(e)("Error in main loop")
        throw e
      case t: Throwable =>
        System.err.println("Fatal error in app")
        t.printStackTrace
        throw t
    }
  }
}
