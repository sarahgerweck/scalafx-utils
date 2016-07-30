package org.gerweck.scalafx.util

import scala.language.implicitConversions

import scalafx.beans.property._

trait LowPriorityImplicits {
  implicit def scalaBooleanToBooleanProperty(ob: ReadOnlyObjectProperty[Boolean]): ReadOnlyBooleanProperty = {
    val b = BooleanProperty(ob.value)
    ob onChange { (_, oldV, newV) =>
      b.value = newV
    }
    b
  }
}
