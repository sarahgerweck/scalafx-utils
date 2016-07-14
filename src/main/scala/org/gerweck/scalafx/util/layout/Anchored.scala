package org.gerweck.scalafx.util.layout

import scalafx.geometry.Insets
import scalafx.scene.control.MenuBar
import scalafx.scene.layout._

/** A wrapper that binds its child pane to all four corners so it will always
  * grow as the stage is resized.
  *
  * This can be useful as the very top element of a [[scalafx.stage.Stage]]
  *
  * @author Sarah Gerweck <sarah.a180@gmail.com>
  */
object Anchored {
  def apply(margins: Insets = Insets.Empty, menus: Option[MenuBar] = None)(body: => Pane) = {
    new AnchorPane { ap =>
      hgrow = Priority.Always
      vgrow = Priority.Always
      private[this] lazy val innerPane = body
      children = menus.toSeq :+ innerPane
      AnchorPane.setAnchors(innerPane, margins.top, margins.right, margins.bottom, margins.left)
    }
  }
}
