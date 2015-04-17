package org.gerweck.scalafx.util

import language.implicitConversions

import scalaz._

import scalafx.beans.property._
import scalafx.beans.value._

trait ObservableImplicits {
  implicit val observableApplicative = new Applicative[Observable] with Functor[Observable] {
    /* Map can be derived from `ap`, but this adds less overhead. */
    override def map[A, B](a: Observable[A])(f: A => B): ObservableValue[B, B] = {
      @inline def recalculate(): B = f(a.value)

      val originalValue = recalculate()

      val prop = ObjectProperty[B](originalValue)

      def changeHandler = {
        prop.value = recalculate()
      }

      a onChange changeHandler
      prop
    }

    def point[A](a: => A): ObservableValue[A, A] = {
      ObjectProperty[A](a)
    }

    def ap[A, B](fa: => Observable[A])(f: => Observable[A => B]): ObservableValue[B, B] = {
      @inline def recalculate(): B = (f.value)(fa.value)

      val originalValue = recalculate()

      val prop = ObjectProperty[B](originalValue)

      var prevValue = originalValue

      def changeHandler = {
        val newVal = recalculate()
        if (prevValue != newVal) {
          prop.value = newVal
        }
      }

      fa onChange changeHandler
      f onChange changeHandler

      prop
    }
  }

  implicit def enrichObservable[A, B](o: ObservableValue[A, B]) = new RichObservable(o)
  implicit def enrichProperty[A, B](o: Property[A, B]) = new RichProperty(o)
}

class RichObservable[A, C](val self: ObservableValue[A, C]) extends AnyVal {
  private type ObjObs[X] = ObservableValue[X, X]
  @inline private def oapp = observableApplicative

  def map[B](f: A => B) = oapp.map(self)(f)
  def <*>[B](f: Observable[A => B]): Observable[B] = oapp.ap(self)(f)
  def tuple[B](f: Observable[B]): Observable[(A,B)] = oapp.tuple2(self, f)
  final def *>[B](fb: ObjObs[B]): Observable[B] = oapp.apply2(self,fb)((_,b) => b)
  final def <*[B](fb: ObjObs[B]): Observable[A] = oapp.apply2(self,fb)((a,_) => a)

  final def |@|[B, B1](fb: ObservableValue[B, B1]) = ObservableTupler(self, fb)

  /** Alias for `|@|` */
  final def ⊛[B, B1](fb: ObservableValue[B, B1]) = |@|(fb)
}

class RichProperty[A, B](val inner: Property[A, B]) extends AnyVal {
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
