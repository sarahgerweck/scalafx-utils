/*
 * Copyright AtScale, Inc. 2015. All Rights Reserved.
 *
 * No part of this project or any of its contents may be reproduced, copied,
 * modified or adapted, without the prior written consent of AtScale, Inc..
 */

package org.gerweck.scalafx.util

/** A property that can be parsed from a TextField.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
trait Parseable[A] {
  val builder: PropertyBuilder[A]

  type Prop = builder.Prop

  def toString(a: A): String = a.toString
  def fromString(s: String): A

  def makeNew(default: A) = builder.makeNew(default)
}

object Parseable {
  private[this] def parseable[A](from: String => A)(implicit build: PropertyBuilder[A]) = new Parseable[A] {
    val builder = build
    def fromString(s: String): A = from(s)
  }
  implicit val IntParseable = parseable[Int](_.toInt)
  implicit val LongParseable = parseable[Long](_.toLong)
  implicit val FloatParseable = parseable[Float](_.toFloat)
  implicit val DoubleParseable = parseable[Double](_.toDouble)
  implicit val BooleanParseable = parseable[Boolean](_.toBoolean)
}
