package gui.services

import java.net.InetSocketAddress

import gui.model.ClientState
import gui.services.LoginService.{ConnectionFailed, NameAlreadyTaken}
import network.{ClientHandler, ClientListener}
import utils.Results.Success

import scala.concurrent.Future
import scalafx.application.Platform

class LoginService(clientHandler: ClientHandler, clientState: ClientState) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def connect(address: InetSocketAddress, name: String): Future[(String, Int)] = {

    connectToServer(address)
      .flatMap(_ => registerUser(name))
  }

  private def connectToServer(address: InetSocketAddress): Future[Success] = {
    if (clientState.connected.value) return Future(Success())

    Platform.runLater {
      clientState.connecting.value = true
    }

    val f: Future[Success] = clientHandler.addListener((onSuccess, onFailure) => new ClientListener {
      override def connected(): Unit = {
        Platform.runLater {
          clientState.connecting.value = false
          clientState.connected.value = true

          onSuccess(Success())
        }
      }

      override def connectionFailed(): Unit = {
        Platform.runLater {
          clientState.connecting.value = false
          clientState.connected.value = false

          onFailure(ConnectionFailed())
        }
      }
    })

    clientHandler.connect(address)

    f
  }

  private def registerUser(name: String): Future[(String, Int)] = {
    if (clientState.registered.value)
      return Future(clientState.name.value -> clientState.id.value)

    Platform.runLater {
      clientState.registering.value = true
    }

    val f: Future[(String, Int)] = clientHandler.addListener((onSuccess, onFailure) => new ClientListener {
      override def registered(name: String, id: Int): Unit = {
        Platform.runLater {
          clientState.registering.value = false
          clientState.registered.value = true
          clientState.id.value = id
          clientState.name.value = name

          onSuccess(name -> id)
        }
      }

      override def nameTaken(name: String): Unit = {
        Platform.runLater {
          clientState.registering.value = false
          clientState.registered.value = false

          onFailure(NameAlreadyTaken())
        }
      }
    })

    clientHandler.register(name)

    f
  }
}

object LoginService {

  case class NameAlreadyTaken() extends Throwable("Name of this user has been alredy taken")

  case class ConnectionFailed() extends Throwable("Connection to the server failed")

}
