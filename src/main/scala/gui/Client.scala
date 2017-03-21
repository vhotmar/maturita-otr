package gui

import java.net.URL
import java.util.{Locale, ResourceBundle}
import javafx.scene.Parent
import javafx.{scene => jfxs}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLLoader}


object Client extends JFXApp with Router with MessageHandler {

  val routes = Map(
    //"chats" -> FXML.load("/Chats.fxml"),
    //"loading" -> FXML.load("/Loading.fxml"),
    "login" -> FXML.load("/Login.fxml")
  )

  val currentRoute = StringProperty("login")

  val styles = List(
    getClass.getResource("/styles.css").toExternalForm,
    "https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic&subset=latin"
  )

  def changeRoute(parent: Parent): Unit = {
    stage.scene = new Scene(parent)
  }

  stage = new JFXApp.PrimaryStage {
    resizable = false
    title.value = "Hello Stage"
    maxWidth = 450
    scene = new Scene(
      route._1
    ) {
      stylesheets = styles
    }
  }
}
