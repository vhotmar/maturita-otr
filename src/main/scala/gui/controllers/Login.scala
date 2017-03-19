package gui.controllers

import scalafx.event.ActionEvent
import scalafx.geometry.Side
import scalafx.scene.control.{ContextMenu, MenuItem, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class Login(
  serverAddressText: TextField,
  userNameText: TextField
) {
  val userNameValidator = new ContextMenu
  userNameValidator.autoHide = false

  val serverAddressValidator = new ContextMenu
  serverAddressValidator.autoHide = false

  def connect(action: ActionEvent) = {
    if (userNameText.text.isEmpty.get) {
      userNameValidator.items.clear()
      userNameValidator.items.add(new MenuItem("Please enter username"))
      userNameValidator.show(userNameText, Side.Right, 10, -10)
    }
  }
}
