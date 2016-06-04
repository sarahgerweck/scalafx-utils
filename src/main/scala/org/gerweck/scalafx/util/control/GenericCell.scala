package org.gerweck.scalafx.util.control

class GenericCell[A, B] extends javafx.scene.control.TableCell[A, B] {
  def handleItemUpdate(item: B) = {
    text = item.toString
  }

  override def updateItem(item: B, empty: Boolean) = {
    super.updateItem(item, empty)
    if (empty || item == null) {
      text = null
      graphic = null
    } else {
      handleItemUpdate(item)
    }
  }

  final def text = getText()
  final def text_=(s: String) = setText(s)
  final def graphic = getGraphic()
  final def graphic_=(g: javafx.scene.Node) = setGraphic(g)
}
