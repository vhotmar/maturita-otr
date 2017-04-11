package gui.services

import gui.services.MessageService.UserDidNotAnswer

import scala.concurrent.{Future, Promise}
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, TextInputDialog}

class MessageService {
  def warn(message: String): Unit = Platform.runLater {
    new Alert(AlertType.Warning) {
      title = "Warn"
      headerText = message
    }.showAndWait()
  }

  // TODO: for now use dialogs, in future would be better in-built UI

  def error(message: String): Unit = Platform.runLater {
    new Alert(AlertType.Error) {
      title = "Error"
      headerText = message
    }.showAndWait()
  }

  def info(message: String): Unit = Platform.runLater {
    new Alert(AlertType.Information) {
      title = "Info"
      headerText = message
    }.showAndWait()
  }

  def text(message: String): Future[String] = {
    val promise = Promise[String]()

    Platform.runLater {
      val dialog = new TextInputDialog() {
        title = "Text"
        headerText = message
      }

      val result = dialog.showAndWait()

      result match {
        case Some(name) => promise.success(name)
        case None => promise.failure(UserDidNotAnswer())
      }
    }

    promise.future
  }

}

object MessageService {

  case class UserDidNotAnswer() extends Throwable("User did not answer")

}