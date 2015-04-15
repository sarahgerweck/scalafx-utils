package org.gerweck.scalafx

import language.implicitConversions

import scalafx.beans.property._
import scalafx.beans.value._
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.util.StringConverter

import scalaz._
import Scalaz._

/** Various implicits and global utilities for ScalaFX work.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
package object util {
  type Observable[A] = ObservableValue[A, _]

  implicit val observableApplicative = new Applicative[Observable] {
    def point[A](a: => A): Observable[A] = {
      ObjectProperty[A](a)
    }

    def ap[A, B](fa: => Observable[A])(f: => Observable[A => B]): Observable[B] = {
      def recalculate: B = (f.value)(fa.value)

      val originalValue = recalculate

      val prop = ObjectProperty[B](originalValue)

      var prevValue = originalValue

      def changeHandler = {
        val newVal = recalculate
        if (prevValue != newVal) {
          prop.value = recalculate
        }
      }

      fa onChange changeHandler
      f onChange changeHandler

      prop
    }
  }

  implicit class RichProperty[A](val inner: Property[A, _]) extends AnyVal {
    def biMap[B <: AnyRef](push: A => B, pull: B => A): ObjectProperty[B] = {
      val original = push(inner.value)
      val op = ObjectProperty[B](original)
      inner onChange {
        val oldVal = op.value
        val newVal = push(inner.value)
        if (oldVal != newVal) {
          op.value = push(inner.value)
        }
      }
      op onChange {
        val oldVal = inner.value
        val newVal = pull(op.value)
        if (oldVal != newVal) {
          inner.value = newVal
        }
      }
      op
    }
  }

  implicit class RichGridPane(val inner: GridPane) extends AnyVal {
    def addToRow(ri: Int, children: Node*) = inner.addRow(ri, children map {_.delegate}: _*)
  }
}
