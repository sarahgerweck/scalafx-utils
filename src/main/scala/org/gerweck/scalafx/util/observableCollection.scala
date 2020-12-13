package org.gerweck.scalafx.util

// import scala.compat.java8.FunctionConverters._

import java.util.function.{ Predicate => JPredicate }

import scalafx.beans.property._
import scalafx.beans.value._
import scalafx.collections.ObservableSet
import scalafx.collections.ObservableArray
import scalafx.collections.ObservableBuffer
import scalafx.collections.transformation.FilteredBuffer
import scala.collection.immutable.ArraySeq

sealed trait ToFlatObservable[-A, +B] extends Calculable[A, B]
object ToFlatObservable extends CalculableObservable[ToFlatObservable[_, _]] {
  implicit def obOps[A] = new ToFlatObservable[ObservableBuffer[A], Seq[A]] {
    override def recalculate(oba: ObservableBuffer[A]) = oba.toVector
  }
  implicit def oaOps[A] = new ToFlatObservable[ObservableArray[A, _, _], Seq[A]] {
    override def recalculate(oaa: ObservableArray[A, _, _]) = {
      ArraySeq.unsafeWrapArray(oaa.toArray)
    }
  }
  implicit def osOps[A] = new ToFlatObservable[ObservableSet[A], collection.immutable.Set[A]] {
    override def recalculate(os: ObservableSet[A]) = os.toSet
  }
}

sealed trait ObservableSized[-A] extends Calculable[A, Int]
object ObservableSized extends CalculableObservable[ObservableSized[_]] {
  implicit def obSize[A] = new ObservableSized[ObservableBuffer[A]] {
    def recalculate(oba: ObservableBuffer[A]) = oba.size
  }
  implicit def oaSize[A] = new ObservableSized[ObservableArray[A, _, _]] {
    def recalculate(oaa: ObservableArray[A, _, _]) = oaa.size
  }
  implicit def osSize[A] = new ObservableSized[ObservableSet[A]] {
    def recalculate(os: ObservableSet[A]) = os.size
  }
}

sealed trait RichObservableSeqLike[A] extends Any {
  def observableSeqValue: ReadOnlyObjectProperty[Seq[A]]
  def observableSize: ReadOnlyObjectProperty[Int]
}

final class RichObservableBuffer[A](val obs: ObservableBuffer[A]) extends AnyVal with RichObservableSeqLike[A] {
  def observableSeqValue: ReadOnlyObjectProperty[Seq[A]] = ToFlatObservable.toObservable(obs)
  def observableSize = ObservableSized.toObservable(obs)

  def observeFiltered(predicate: A => Boolean) = {
    new FilteredBuffer(obs, predicate)
  }
  /* The dummy implicit is here to ensure the `observeFiltered` methods all have different post-erasure types */
  def observeFiltered[B >: A](predicate: ObservableValue[JPredicate[B], JPredicate[B]])(implicit dummy: DummyImplicit): FilteredBuffer[A] = {
    val fb = new FilteredBuffer(obs)
    fb.predicate <== predicate
    fb
  }
  // def observeFiltered[B >: A](predicate: Observable[B => Boolean]): FilteredBuffer[A] = {
  //   observeFiltered(predicate.map[JPredicate[A]](asJavaPredicate))
  // }
}

final class RichObservableArray[A, B <: ObservableArray[A, B, C], C <: javafx.collections.ObservableArray[C]](val oaa: ObservableArray[A, B, C]) extends AnyVal with RichObservableSeqLike[A] {
  def observableSeqValue: ReadOnlyObjectProperty[Seq[A]] = ToFlatObservable.toObservable(oaa)
  def observableSize = ObservableSized.toObservable(oaa)
}

final class RichObservableSet[A](val os: ObservableSet[A]) extends AnyVal {
  def observableSetValue: ReadOnlyObjectProperty[Set[A]] = ToFlatObservable.toObservable(os)
  def observableSize = ObservableSized.toObservable(os)
}

class CalculableObservable[O <: Calculable[_, _]] {
  final def toObservable[A, B](a: A)(implicit ops: O with Calculable[A, B], cl: ChangeListenable[A]): ReadOnlyObjectProperty[B] = {
    @inline def recalculate(): B = ops.recalculate(a)
    val originalValue = recalculate()
    val prop = ObjectProperty[B](originalValue)
    var prevValue = originalValue
    cl.onChange(a) {
      prop.synchronized {
        val newVal = recalculate()
        if (prevValue != newVal) {
          prop.value = newVal
          prevValue = newVal
        }
      }
    }
    prop
  }
}

/* Type Classes */

trait Calculable[-A, +B] extends Any {
  def recalculate(a: A): B
}

sealed trait ChangeListenable[-A] {
  def onChange(a: A)(b: => Unit): Unit
}
object ChangeListenable {
  implicit def obListenable[A]: ChangeListenable[ObservableBuffer[A]] = new ChangeListenable[ObservableBuffer[A]] {
    def onChange(oba: ObservableBuffer[A])(b: => Unit) = oba onChange b
  }
  implicit def oaListenable[A]: ChangeListenable[ObservableArray[A, _, _]] = new ChangeListenable[ObservableArray[A, _, _]] {
    def onChange(oaa: ObservableArray[A, _, _])(b: => Unit) = oaa onChange b
  }
  implicit def osListenable[A]: ChangeListenable[ObservableSet[A]] = new ChangeListenable[ObservableSet[A]] {
    def onChange(osa: ObservableSet[A])(b: => Unit) = osa onChange b
  }
}

sealed trait DeriveChanges[A] {
  protected val evChange: ChangeListenable[A]
  def onChange(a: A)(b: => Unit) = evChange.onChange(a)(b)
}
