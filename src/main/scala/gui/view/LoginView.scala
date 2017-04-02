package gui.view

import gui.internal.ui.ErrorsManager
import gui.view.model.LoginViewModel

import scalafx.event.ActionEvent
import scalafx.scene.control.TextField
import scalafx.scene.layout.StackPane
import scalafxml.core.macros.sfxml

@sfxml
class LoginView(
                 serverAddressText: TextField,
                 userNameText: TextField,
                 loading: StackPane,
                 model: LoginViewModel
               ) {
  userNameText.text <==> model.userName
  serverAddressText.text <==> model.address

  loading.visible <== model.loading

  val manager = ErrorsManager(model.errors, Map(
    "userName" -> userNameText,
    "address" -> serverAddressText
  ))

  def connect(action: ActionEvent): Unit = model.connect()
}
