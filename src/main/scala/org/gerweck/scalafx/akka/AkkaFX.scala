package org.gerweck.scalafx.akka

import scala.concurrent.Future

import akka.Done
import akka.stream.scaladsl._

import scalafx.application.Platform.runLater
import scalafx.beans.property.Property

/** A master object that exposes all the Akka-ScalaFX bridges.
  *
  * @author Sarah Gerweck <sarah.a180@gmail.com>
  */
object AkkaFX extends AkkaStreamFX

trait AkkaStreamFX {
  /** A [[akka.stream.scaladsl.Sink]] that sends all values to a
    * [[scalafx.beans.property.Property]].
    *
    * Each event that's written into the `Sink` will trigger an update of the
    * `Property` with the streamed value.
    */
  def observableSink[A](prop: Property[A, _]): Sink[A, Future[Done]] = {
    Sink.foreach[A] { a =>
      runLater {
        prop.value = a
      }
    }
  }
}
