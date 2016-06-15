package org.gerweck.scalafx.util.control

import language.implicitConversions

import scalafx.geometry.Orientation.Horizontal
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.{ Label, Separator }
import scalafx.scene.layout._
import scalafx.scene.text._

import org.gerweck.scalafx.util._

import ControlPanel._

object HorizontalControlPanel {
  def apply(controls: ControlPanelEntry*): Pane =
    new HBox {
      alignment = Pos.Center
      hgrow = Priority.Always
      spacing = 15
      children = controls map {
        case RegularControl(name, control) =>
          new HBox {
            spacing = 4
            hgrow = Priority.Always
            children = ControlPanel.controlRow(name, control)
            alignment = Pos.CenterRight
          }
        case SeparatorEntry =>
          Separators.vertical
      }
    }
}

object VerticalControlPanel {
  def apply(controls: ControlPanelEntry*): Pane = new GridPane { gp =>
    hgap = 4
    vgap = 6
    for ((entry, i) <- controls.zipWithIndex) {
      entry match {
        case RegularControl(name, control) =>
          gp.addToRow(i, ControlPanel.controlRow(name, control): _*)

        case SeparatorEntry =>
          val sep = new Separator {
            orientation = Horizontal
          }
          gp.add(sep, 0, i, 2, 1)
      }
    }
  }
}

object ControlPanel {
  def apply(controls: ControlPanelEntry*): Pane = VerticalControlPanel(controls: _*)

  private[this] lazy val defaultFont = new Font(Font.default)
  private[this] lazy val boldFont = {
    Font(defaultFont.family, FontWeight.Bold, defaultFont.size)
  }

  private[this] lazy val labelsFont = defaultFont

  private[control] def controlRow(name: String, control: Node) = {
    val label = new Label(name + ':') {
      alignmentInParent = Pos.CenterRight
      font = labelsFont
    }
    Seq(label, control)
  }

  /* It's expected that you won't create these directly, but that you'll
   * mostly use the implicit magnets */
  sealed trait ControlPanelEntry
  case class RegularControl(label: String, control: Node) extends ControlPanelEntry
  case object SeparatorEntry extends ControlPanelEntry

  object ControlPanelEntry {
    implicit def pair2Regular(p: (String, Node)): RegularControl = RegularControl.tupled(p)
    implicit def sep2Sep(p: Separator): SeparatorEntry.type = SeparatorEntry
  }
}
