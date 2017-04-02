package gui.internal.ui

import scalafx.Includes._
import scalafx.scene.control.{Label, PopupControl, TextField}
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.StackPane

class ValidationControl(textField: TextField) extends PopupControl {
  val label = new Label("") {
    styleClass += "label"
  }

  val pane = new StackPane() {
    styleClass += "validation"
    children = Seq(
      label
    )
  }

  autoHide = false
  hideOnEscape = false

  scene.value.setRoot(pane)

  onShown = handle {
    textField.styleClass += "error"
  }

  onHidden = handle {
    textField.styleClass = textField.styleClass.filter(_ != "error")
  }

  textField.onKeyTyped = (keyEvent: KeyEvent) => {
    if (showing.value && keyEvent.getCharacter != KeyEvent.CharUndefined)
      hide()
  }

  pane.layoutBounds.onChange(invalidate)

  pane.onMouseClicked = handle {
    hide()
  }
  pane.onTouchReleased = handle {
    hide()
  }

  def show(message: String): Unit = {
    label.text = message

    invalidate()
  }

  def invalidate(): Unit = {
    val (x, y) = calculate

    if (!showing.value)
      super.show(textField, x, y)
    else {
      delegate.setAnchorX(x)
      delegate.setAnchorY(y)
    }
  }

  def calculate: (Double, Double) = {
    val del = pane.delegate
    val node = textField

    del.applyCss()

    val bounds = node.localToScreen(node.getLayoutBounds)

    val x = bounds.getMaxX + 10
    val y = bounds.getMinY + bounds.getHeight / 2 - del.prefHeight(-1) / 2 - 1

    (x, y)
  }
}
