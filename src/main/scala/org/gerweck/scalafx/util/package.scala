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
  type SimpleProperty[A] = Property[A, _]

  implicit val observableFunctor = new Functor[Observable] {
    def map[A, B](a: Observable[A])(f: A => B): Observable[B] = {
      @inline def recalculate(): B = f(a.value)

      val originalValue = recalculate()

      val prop = ObjectProperty[B](originalValue)

      def changeHandler = {
        prop.value = recalculate()
      }

      a onChange changeHandler
      prop
    }

  }

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

  implicit class RichObservable[A](val self: Observable[A]) {
    private type F[X] = Observable[X]
    @inline private def F = observableApplicative

    def map[A1 >: A, B](f: A1 => B) = F.map(self)(f)
    def <*>[B](f: Observable[A => B]): Observable[B] = F.ap(self)(f)
    def tuple[B](f: Observable[B]): Observable[(A,B)] = F.tuple2(self, f)
    final def *>[B](fb: F[B]): F[B] = F.apply2(self,fb)((_,b) => b)
    final def <*[B](fb: F[B]): F[A] = F.apply2(self,fb)((a,_) => a)

    import shapeless._
    import shapeless.syntax._
    import shapeless.ops.hlist._
    import HList._
    implicitly[HListOps[Int::Int::HNil]]
    final def |@|[B](fb: F[B]) = new ObservableTupler(self::fb::HNil)

    /** Alias for `|@|` */
    final def ⊛[B](fb: F[B]) = |@|(fb)
  }

  object unwrapObservable extends Poly1 {
    implicit def apply[T, A <% Observable[T]]: Case.Aux[A, T] = at[A]{ o => o.value }
  }

  trait TupleBuilder[]

  implicit class RichProperty[A](val inner: SimpleProperty[A]) extends AnyVal {
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
