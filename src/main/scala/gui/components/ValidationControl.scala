package gui.components

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

  def show(message: String) = {
    label.text = message

    invalidate
  }

  def invalidate = {
    val (x, y) = calculate

    super.show(textField, x, y)
  }

  def calculate = {
    val del = pane.delegate
    val node = textField

    del.applyCss()

    val bounds = node.localToScreen(node.getLayoutBounds)

    val x = bounds.getMaxX + 10
    val y = bounds.getMinY + bounds.getHeight / 2 - del.prefHeight(-1) / 2 - 1

    (x, y)
  }
}
