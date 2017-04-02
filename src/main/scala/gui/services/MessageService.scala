package gui.services

import scalafx.application.Platform
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

class MessageService {
  // TODO: for now use dialogs, in future would be better in-built UI

  def error(message: String): Unit = Platform.runLater {
    new Alert(AlertType.Error) {
      title = "Error"
      headerText = message
    }.showAndWait()
  }
}
