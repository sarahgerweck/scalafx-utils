package org.gerweck.scalafx.util

import scala.concurrent._
import scala.util._

import org.log4s._

import scalafx.application.Platform.runLater
import scalafx.beans.property._

/** An [[scalafx.beans.value.ObservableValue]] that pulls its value from a future.
  *
  * @author Sarah Gerweck <sarah.a180@gmail.com>
  */
object FutureObservable {
  private[this] val logger = getLogger

  /** Construct an observable that gives the result of [[scala.concurrent.Future.value]] at all
    * times.
    *
    * Like the underlying method, it gives `None` until the `Future` completes, after which it
    * gives a `Some` of the `Try` that describes the calculation.
    */
  def ofTryOption[A](future: Future[A])(implicit ec: ExecutionContext): ReadOnlyObjectProperty[Option[Try[A]]] = {
    val startValue = future.value
    val prop = ObjectProperty(startValue)
    if (startValue.isEmpty) {
      future onComplete { result =>
        runLater {
          prop.value = Some(result)
        }
      }
    }
    prop
  }

  /* NOTE: All the other methods below ''could'' be derived from this one, but
   * it's better to use as few JavaFX callbacks as possible since they have to
   * be executed in the main UI thread.
   */

  /** Construct an observable that gives the value of the future on success.
    *
    * Until the future completes successfully, the value will be that of
    * `defaultValue`. If there is an error, the value will persist as
    * `defaultValue`.
    *
    * If you want to change state in case of an error, I recommend you use
    * [[scala.concurrent.Future.recover]] to choose the values that will be
    * used in that case. The `defaultValue` is provided because `Future` has
    * no equivalent mechanism for this mandatory functionality, but recovery
    * is already a built-in feature.
    */
  def withDefault[A](defaultValue: A)(future: Future[A])(implicit ec: ExecutionContext): ReadOnlyObjectProperty[A] = {
    future.value match {
      case Some(Success(a)) =>
        ObjectProperty(a)

      case Some(Failure(f)) =>
        logFailure(f)
        ObjectProperty(defaultValue)

      case None =>
        val prop = ObjectProperty[A](defaultValue)
        future onComplete {
          case Success(a) =>
            runLater {
              prop.value = a
            }
          case Failure(f) =>
            logFailure(f)
        }
        prop
    }
  }

  @deprecated("Use withDefault. `Apply` will be either removed or repurposed in a future release.", "0.10.2")
  def apply[A](defaultValue: A)(future: Future[A])(implicit ec: ExecutionContext) = withDefault(defaultValue)(future)

  /** Construct an observable that gives `None` until the `Future` completes successfully, after
    * which the `Option` contains the successful result.
    *
    * This method does not allow you to differentiate between a failure and a calculation
    * that is still running. If you need to differentiate these, you can either use
    * [[scala.concurrent.Future.recover]] or use [[ofTryOption]] instead.
    */
  def ofSuccessOption[A](future: Future[A])(implicit ec: ExecutionContext): ReadOnlyObjectProperty[Option[A]] = {
    future.value match {
      case Some(Success(a)) =>
        ObjectProperty(Some(a))

      case Some(Failure(f)) =>
        logFailure(f)
        ObjectProperty(None)

      case None =>
        val prop = ObjectProperty[Option[A]](None)
        future onComplete {
          case Success(a) =>
            runLater {
              prop.value = Some(a)
            }
          case Failure(f) =>
            logFailure(f)
        }
        prop
    }
  }

  private[this] def logFailure(f: Throwable): Unit = {
    logger.debug(s"Got failure from FutureObservable's result: $f")
  }
}
