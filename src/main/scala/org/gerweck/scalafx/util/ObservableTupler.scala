package org.gerweck.scalafx.util

import language.existentials

import scalaz._
import Scalaz._

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.function._

import scalafx.beans.property.ObjectProperty
import scalafx.beans.value.ObservableValue

/**
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class ObservableTupler
    [HLObs <: HList, HLParams <: HList, TParams <: Product] private
    (hlist: HLObs)
    (implicit unwrapper: Mapper.Aux[ObservableUnwrapper.type, HLObs, HLParams],
              tupler: Tupler.Aux[HLParams, TParams],
              lister: ToList[HLObs, Observable[_]]) {

  def |@|[O, P, Appended <: HList, Unwrapped <: HList, Tupled <: Product, ApList]
         (f: ObservableValue[O, P])
         (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                   uw: Mapper.Aux[ObservableUnwrapper.type, Appended, Unwrapped],
                   tplr: Tupler.Aux[Unwrapped, Tupled],
                   lst: ToList[Appended, Observable[_]]): ObservableTupler[Appended, Unwrapped, Tupled] = {
    val newHL: Appended = hlist :+ f
    new ObservableTupler[Appended, Unwrapped, Tupled](newHL)
  }

  def hlisted: ObservableValue[HLParams, HLParams] = {
    def calculate() = unwrapper(hlist)
    val original = calculate()
    val prop = ObjectProperty[HLParams](original)

    for {
      component <- hlist.toList
    } {
      component onChange {
        prop.value = calculate()
      }
    }
    prop
  }

  def tupled: ObservableValue[TParams, TParams] = {
    def calculate() = unwrapper(hlist).tupled

    val original = calculate()
    val prop = ObjectProperty[TParams](original)

    for {
      component <- hlist.toList
    } {
      component onChange {
        prop.value = calculate()
      }
    }
    prop
  }

  def apply[Func, Result](f: Func)(implicit ffp: FnFromProduct.Aux[HLParams => Result, Func], ftp: FnToProduct.Aux[Func, HLParams => Result]): ObservableValue[Result, Result] = {
    hlisted map ftp(f)
  }
}

object ObservableUnwrapper extends Poly1 {
  implicit def apply[T, U, A](implicit ev1: A => ObservableValue[T, U]): Case.Aux[A, T] = at[A]{ o => o.value }
}

object ObservableTupler {
  def apply[A, A1, B, B1](a: ObservableValue[A, A1], b: ObservableValue[B, B1]) = {
    new ObservableTupler(a::b::HNil)
  }
}
