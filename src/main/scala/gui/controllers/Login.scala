package gui.controllers

import java.net.{InetSocketAddress, URI, URISyntaxException}

import gui._
import gui.components.ValidationControl
import network.{ClientHandler, ClientListener}
import otr.ValidationError
import utils.Results.{FResult, FTry}

import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafxml.core.macros.sfxml
import otr.utils.Validate._

import scalaz._
import Scalaz._
import scalafx.geometry.Insets
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import scalafx.scene.paint.{Color, Paint}

@sfxml
class Login(
  serverAddressText: TextField,
  userNameText: TextField,
  loading: StackPane,
  clientHandler: ClientHandler,
  router: Router,
  messages: MessageHandler,
  state: GUIState
) extends Route with ClientListener {
  val userNameValidation = new ValidationControl(userNameText)
  val serverAddressValidation = new ValidationControl(serverAddressText)

  loading.visible <== state.connecting || state.registering

  override def enter(): RouteResult = {
    clientHandler.addListener(this)

    Continue()
  }

  override def leave(): Unit = {
    clientHandler.removeListener(this)
  }

  override def connected(): Unit = {
    clientHandler.register(userNameText.text.value)
  }

  override def connectionFailed(): Unit = {
    messages.error("Connection to specified server failed")
  }

  override def registered(name: String, id: Int): Unit = {
    router.redirect("chats")
  }

  override def nameTaken(name: String): Unit = {
    messages.error("This user name is already taken")
  }

  def connect(action: ActionEvent) = {
    val valid = List(
      validate(userNameText, userNameValidation, validateText),
      validate(serverAddressText, serverAddressValidation, validateURI)
    )

    if (valid.all(x => x)) {
      getURI(serverAddressText.text.value)
        .foreach(uri => {
          // if we have already connected, than just register
          if (!state.connected.value)
            clientHandler
              .connect(new InetSocketAddress(uri._1, uri._2))
          else
            clientHandler
              .register(userNameText.text.value)
        })
    }
  }

  def validate(field: TextField, validation: ValidationControl, method: (String) => Option[String]): Boolean = {
    val error = method(field.text.value)

    if (error.isDefined) {
      validation.show(error.get)

      false
    } else {
      true
    }
  }

  def validateText(str: String): Option[String] = {
    if (str.isEmpty)
      Some("Missing user name")
    else
      None
  }

  def validateURI(str: String): Option[String] = {
    val errors = for {
      _ <- (!str.isEmpty).validate("Missing server address")
      uri <- getURI(str).leftMap(_ => ValidationError("Server address does not have correct format"))
    } yield true

    errors.swap.toOption.map({
      case ValidationError(msg) => msg
      case _ => "Invalid server address"
    })
  }

  def getURI(str: String) = FTry {
    val uri = new URI("my://" + str)
    val host = uri.getHost
    val port = uri.getPort

    if (host == null || port == -1)
      throw new URISyntaxException(uri.toString,
        "URI must have host and port parts")

    (host, port)
  }
}
