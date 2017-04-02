package gui.view.model

import java.net.{InetSocketAddress, URI, URISyntaxException}
import java.util.ResourceBundle

import gui.internal.InputValidation._
import gui.internal.router.Router
import gui.services.{LoginService, MessageService}
import utils.Results.FTry

import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableMap
import scalaz.Scalaz._

class LoginViewModel(
                      resourceBundle: ResourceBundle,
                      loginService: LoginService,
                      model: gui.model.State,
                      router: Router,
                      messageService: MessageService
                    ) {
  val userName = StringProperty("1234")
  val address = StringProperty("localhost:8765")
  val loading = BooleanProperty(false)

  loading <== model.connecting or model.registering

  val errors: ObservableMap[String, String] = ObservableMap.empty[String, String]

  def connect(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    errors.clear()

    validate().foreach(p => errors += p)

    if (errors.isEmpty) {
      getAddress(address.value)
        .foreach(x => {
          val f = loginService
            .connect(x, userName.value)

          f.onSuccess({
            case _ => router.redirect("chats")
          })

          f.onFailure({
            case LoginService.NameAlreadyTaken() =>
              messageService.error("This name was already taken by somebody else")

            case e =>
              messageService.error("Cannot connect to the server")

              println(e)
          })
        })
    }
  }

  def validate(): Map[String, String] = {
    def nonEmpty(msg: String): Vali[String, String] =
      Vali((x: String) =>
        (!x.isEmpty)
          .option(x)
          .toSuccess(msg))

    def validUri(msg: String): Vali[String, String] =
      Vali((x: String) =>
        getAddress(x).toOption.map(_ => x).toSuccess(msg))

    val errors = Map(
      "userName" -> (
        userName.value
          |> nonEmpty("gui.login.usernameMissing")),
      "address" ->
        (address.value
          |> nonEmpty("gui.login.addressMissing")
          >=> validUri("gui.login.addressInvalid"))
    )

    errors
      .filter(_._2.isFailure)
      .mapValues(_.fold(e => e, _ => ""))
  }

  def getAddress(str: String) = FTry {
    val uri = new URI("my://" + str)
    val host = uri.getHost
    val port = uri.getPort

    if (host == null || port == -1)
      throw new URISyntaxException(uri.toString,
        "URI must have host and port parts")

    new InetSocketAddress(host, port)
  }
}

