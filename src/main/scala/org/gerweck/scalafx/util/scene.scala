package org.gerweck.scalafx.util

import scalafx.geometry.Orientation._
import scalafx.scene.control._

object Separators {
  def vertical() = new Separator { orientation = VERTICAL }
  def horizontal() = new Separator { orientation = HORIZONTAL }
}

