package org.gerweck.scalafx.util.control

import scalafx.geometry.Pos
import scalafx.scene.control.TableCell

class CenteredTextCell[A, B] extends TableCell[A, B](new GenericCell[A, B]) {
  alignment = Pos.Center
}
