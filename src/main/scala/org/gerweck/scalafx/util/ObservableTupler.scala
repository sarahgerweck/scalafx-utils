/*
 * Copyright AtScale, Inc. 2015. All Rights Reserved.
 *
 * No part of this project or any of its contents may be reproduced, copied,
 * modified or adapted, without the prior written consent of AtScale, Inc..
 */

package org.gerweck.scalafx.util

import language.existentials

import scalaz._
import Scalaz._

import shapeless._
import shapeless.syntax._
import shapeless.ops.hlist._

import scalafx.beans.property.ObjectProperty
import scalafx.beans.value.ObservableValue

import ObservableTupler._

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

  def ap[O, P, Appended <: HList]
        (f: ObservableValue[O, P])
        (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended]) = {
    hlist :+ f
  }

  def uw[O, P, Appended <: HList, Unwrapped <: HList]
        (f: ObservableValue[O, P])
        (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                  uw: Mapper.Aux[ObservableUnwrapper.type, Appended, Unwrapped]): Unwrapped = {
    uw(hlist :+ f)
  }

  def tp[O, P, Appended <: HList, Unwrapped <: HList, Tupled <: Product]
         (f: ObservableValue[O, P])
         (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                   uw: Mapper.Aux[ObservableUnwrapper.type, Appended, Unwrapped],
                   tplr: Tupler.Aux[Unwrapped, Tupled]): Tupled = {
    uw(hlist :+ f).tupled
  }

  def tl[O, P, Appended <: HList, Unwrapped <: HList, Tupled <: Product, ApList]
        (f: ObservableValue[O, P])
        (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                  uw: Mapper.Aux[ObservableUnwrapper.type, Appended, Unwrapped],
                  tplr: Tupler.Aux[Unwrapped, Tupled],
                  lst: ToList[Appended, Observable[_]]): Tupled = {
    val hl = hlist :+ f
    hl.toList
    uw(hl).tupled
  }

  def |@|[O, P, Appended <: HList, Unwrapped <: HList, Tupled <: Product, ApList]
         (f: ObservableValue[O, P])
         (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                   uw: Mapper.Aux[ObservableUnwrapper.type, Appended, Unwrapped],
                   tplr: Tupler.Aux[Unwrapped, Tupled],
                   lst: ToList[Appended, Observable[_]]): ObservableTupler[Appended, Unwrapped, Tupled] = {
    val newHL: Appended = hlist :+ f
    new ObservableTupler[Appended, Unwrapped, Tupled](newHL)
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

  def apply[C](f: TParams => C): Observable[C] = tupled map f
}

object ObservableUnwrapper extends Poly1 {
  implicit def apply[T, U, A <% ObservableValue[T, U]]: Case.Aux[A, T] = at[A]{ o => o.value }
}

object ObservableTupler {

  def apply[A, A1, B, B1](a: ObservableValue[A, A1], b: ObservableValue[B, B1]) = {
    new ObservableTupler(a::b::HNil)
  }
}
