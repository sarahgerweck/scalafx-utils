package org.gerweck.scalafx.util
package control

import org.log4s._

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.Stage
import javafx.scene.{ Node => JNode }
import javafx.scene.layout.{ Region => JRegion }

/** A standard button bar with the major dialog buttons.
  *
  * This will show the Okay, Cancel and Apply buttons, as well as optionally
  * including a separator.
  */
class DialogButtons(stage: Stage, showOk: Boolean = true, showApply: Boolean = true, showCancel: Boolean = true) extends Region(new DialogButtons.DialogButtonsJFXRegion) {
  protected[this] val logger = getLogger

  def insetSize = 10d
  def buttonInsets = Insets(insetSize)

  def applyChanges(): Unit = {}

  protected[this] def typedButton(bt: ButtonType) = {
    val b = new Button(bt.text) {
      padding = buttonInsets
    }
    ButtonBar.setButtonData(b, bt.buttonData)
    b
  }
  lazy val okButton = {
    val b = typedButton(ButtonType.OK)
    b.onAction = { ae: ActionEvent =>
      logger.debug(s"OK button pressed")
      applyChanges()
      stage.close()
    }
    b
  }
  lazy val cancelButton = {
    val b = typedButton(ButtonType.Close)
    b.onAction = { ae: ActionEvent =>
      logger.debug("Cancel button pressed")
      stage.close()
    }
    b
  }
  lazy val applyButton = {
    val b = typedButton(ButtonType.Apply)
    b.onAction = { ae: ActionEvent =>
      logger.debug("Apply button pressed")
      applyChanges()
    }
    b
  }
  private[this] def seqIf[A](gate: Boolean, a: => A): Seq[A] = {
    if (gate) Seq(a) else Seq.empty
  }
  def bar = new ButtonBar {
    padding = buttonInsets
    buttons = {
      seqIf(showApply, applyButton) ++
      seqIf(showOk, okButton) ++
      seqIf(showCancel, cancelButton)
    }
  }

  protected[control] def mainLayout = new VBox(Separators.horizontal(), bar) {
    hgrow = Priority.Always
  }

  delegate.asInstanceOf[DialogButtons.DialogButtonsJFXRegion].addChild(mainLayout)
}

object DialogButtons {
  private class DialogButtonsJFXRegion extends JRegion {
    protected[control] def addChild(n: JNode) = {
      getChildren.add(n)
    }
  }

  def withCallback(stage: Stage, showOk: Boolean = true, showApply: Boolean = true, showCancel: Boolean = true)(applyChanges: () => Unit): Node = {
    val ac = applyChanges
    val db = new DialogButtons(stage, showOk, showApply, showCancel) {
      override def applyChanges() = ac()
    }
    db.mainLayout
  }
}
