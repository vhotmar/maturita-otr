package gui

import javafx.scene.Parent

import gui.internal.ViewLoader
import gui.internal.router._
import gui.model.ClientState

import scalafx.application.Platform
import scalafx.stage.Stage


class ClientRouter(loader: ViewLoader, clientState: ClientState) extends Router {
  val chatsRoute = new Route {
    override def enter(stage: Stage): RouteResult = {
      if (!clientState.registered.value) Redirect("login")
      else {
        Platform.runLater {
          stage.resizable = true
          stage.width = 800
          stage.height = 500
        }

        Continue()
      }
    }
  }

  val loginRoute = new Route {
    override def enter(stage: Stage): RouteResult = {
      if (clientState.registered.value) Redirect("chats")
      else {
        Platform.runLater {
          stage.resizable = false
          stage.width = 300
        }

        Continue()
      }
    }
  }
  protected var currentRoute: String = "login"

  def initRoutes(): Map[String, (Parent, Route)] = {
    Map(
      "login" -> (loader.load("/Login.fxml")._1, loginRoute),
      "chats" -> (loader.load("/Chats.fxml")._1, chatsRoute)
    )
  }
}
