/*
 * Copyright AtScale, Inc. 2015. All Rights Reserved.
 *
 * No part of this project or any of its contents may be reproduced, copied,
 * modified or adapted, without the prior written consent of AtScale, Inc..
 */

package org.gerweck.scalafx.util

import scalaz._
import Scalaz._

import shapeless._
import shapeless.syntax._
import shapeless.ops.hlist._

import scalafx.beans.value.ObservableValue

/**
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class ObservableTupler[HLObs <: HList, HLParams <: HList, TParams <: Product] private[util]
    (hlist: HLObs)
    (implicit ops: HListOps[HLObs],
              unwrapper: Mapper.Aux[unwrapObservable.type, HLObs, HLParams],
              tupler: Generic.Aux[HLParams, TParams]) {
  def |@|[O, P, Appended <: HList, Unwrapped, Tupled]
         (f: ObservableValue[O, P])
         (implicit prepend: Prepend.Aux[HLObs, ObservableValue[O, P]::HNil, Appended],
                   uw: Mapper.Aux[unwrapObservable.type, Appended, Unwrapped],
                   tplr: Generic.Aux[Unwrapped, Tupled],
                   ops: HListOps[Appended]) = {
    val newHL: Appended = hlist :+ f
    new ObservableTupler[Appended, Unwrapped, Tupled](newHL)
  }
}


object ObservableTupler {
  object unwrapObservable extends Poly1 {
    implicit def apply[T, U, A <% ObservableValue[T, U]]: Case.Aux[A, T] = at[A]{ o => o.value }
  }
}
