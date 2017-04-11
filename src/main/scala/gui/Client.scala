package gui

import java.security.Security
import javafx.application.Platform

import gui.internal.router.Router
import org.bouncycastle.jce.provider.BouncyCastleProvider

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.Includes._

object Client extends JFXApp {

  import scaldi.Injectable._

  Platform.setImplicitExit(false)

  Security.addProvider(new BouncyCastleProvider())

  implicit val module = new ClientModule()

  val router = inject[Router]


  val styles = List(
    getClass.getResource("/styles.css").toExternalForm,
    "https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic&subset=latin"
  )

  stage = new JFXApp.PrimaryStage {
    resizable = false

    title.value = "OTR"

    scene = new Scene(router) {
      stylesheets = styles
    }
  }

  router.init(stage)

  stage.onCloseRequest = handle {
    Platform.exit()
  }

  override def stopApp(): Unit = {
    super.stopApp()

    module.destroy()
  }
}
