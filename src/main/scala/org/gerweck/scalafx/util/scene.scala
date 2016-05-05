package org.gerweck.scalafx.util

import scalafx.geometry.Orientation.{ Horizontal, Vertical }
import scalafx.scene.control._

object Separators {
  def vertical() = new Separator { orientation = Vertical }
  def horizontal() = new Separator { orientation = Horizontal }
}

