package org.gerweck.scalafx

import language.implicitConversions
import language.existentials

import scalafx.beans.property._
import scalafx.beans.value._
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.util.StringConverter

import scalaz._
import Scalaz._

/** Various implicits and global utilities for ScalaFX work.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
package object util extends ObservableImplicits {
  type Observable[A] = ObservableValue[A, _]
  type SimpleProperty[A] = Property[A, _]


  object TextDisplay {
    import scalafx.beans.property._
    import scalafx.scene.text.Text
    def apply(text: ObservableValue[String,String]) = {
      val t = new scalafx.scene.text.Text
      t.text <== text
      t
    }
  }

  implicit class RichGridPane(val inner: GridPane) extends AnyVal {
    def addToRow(ri: Int, children: Node*) = inner.addRow(ri, children map {_.delegate}: _*)
  }
}
