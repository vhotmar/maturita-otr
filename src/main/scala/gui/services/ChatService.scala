package gui.services

import _root_.utils.Results
import _root_.utils.Results.FResult
import gui.model.{ChatMessage, ChatState, ChatsState, ClientState}
import gui.services.ChatService.{AlreadyConnectedTo, ConnectionWithIdDoesNotExists, UserDoesNotExists}
import gui.services.ClientService.ClientIsNotInitialized
import network.{ClientHandler, ClientListener}
import otr.{Client, ClientData, Sender}

import scala.concurrent.Future
import scalafx.application.Platform
import scalaz.Scalaz._

class ChatService(
                   clientHandler: ClientHandler,
                   chatsState: ChatsState,
                   clientState: ClientState,
                   messageService: MessageService
                 ) {

  import scala.concurrent.ExecutionContext.Implicits.global

  clientHandler.addListener(new ClientListener {
    override def connectionClosed(): Unit = Platform.runLater {
      chatsState.chats.clear()

    }

    override def connectionFromUser(name: String, id: Int): Unit = Platform.runLater {
      addChat(name, id)
    }

    override def receivedMessage(id: Int, message: Array[Byte]): Unit = Platform.runLater {
      val s = for {
        chat <- getChat(id)
        message <- chat.client.receive(message)
      } yield Results.Success()
    }

    override def disconnected(id: Int): Unit = Platform.runLater {
      if (chatsState.chats.contains(id)) {
        if (chatsState.currentChatId.value == id) {
          chatsState.currentChatId.value = {
            if (chatsState.chats.size <= 1)
              -1
            else
              chatsState.chats.head._1
          }

          chatsState.chats.remove(id)
        } else {
          chatsState.chats.remove(id)
        }
      }
    }
  })

  def connectTo(name: String): Future[(String, Int)] = {
    if (!clientState.ready.value) return Future.failed(ClientIsNotInitialized())

    val chat = chatsState.chats.find(x => x._2.name == name)

    if (chat.isDefined) return Future.failed(AlreadyConnectedTo(name))

    val f: Future[(String, Int)] = clientHandler.addListener((onSuccess, onFailure) => new ClientListener {
      override def connectedToUser(name: String, id: Int): Unit = {
        onSuccess(name -> id)

        addChat(name, id, init = true)
      }

      override def userDoesNotExists(name: String): Unit = {
        onFailure(UserDoesNotExists(name))
      }
    })

    clientHandler.connectTo(name)

    f
  }

  private def addChat(name: String, id: Int, init: Boolean = false) = Platform.runLater {
    val chat = Client.create(
      new Sender {
        override def send(bytes: Array[Byte]): Unit = {
          clientHandler.sendMessage(id, bytes)
        }
      },
      new otr.ClientListener {
        override def smpRequestReceived(question: Option[String]): Unit = {
          val future = messageService
            .text(s"User $name requested proof of your identity by using password ${question.fold("")(x => s"($x)")}")

          future.onFailure({
            case _ => abortSmp(id)
          })

          future.onSuccess({
            case s: String => answerSmp(id, s)
          })
        }

        override def receive(bytes: Array[Byte]) = {
          receiveMessage(id, new String(bytes))
        }

        override def smpResult(res: Boolean): Unit = {
          if (res)
            messageService.info(s"Proof of identity with $name was successful")
          else
            messageService.warn(s"Proof of identity with $name was unsuccessful")
        }

        override def smpAbort(): Unit = {
          messageService.warn(s"Proof of identity with $name was aborted")
        }
      },
      ClientData(clientState.name.value), init)

    chat.fold(x => println(x), client => {
      if (init)
        client.init()

      chatsState.chats += id -> ChatState(id, name, client)

      if (chatsState.currentChatId.value == -1)
        chatsState.currentChatId.value = id
    })
  }

  def answerSmp(id: Int, secret: String): FResult[Results.Success] = {
    for {
      chat <- getChat(id)

      _ = chat.client.answerSmp(secret.getBytes)
    } yield utils.Results.Success()
  }

  def abortSmp(id: Int): FResult[Results.Success] = {
    for {
      chat <- getChat(id)

      _ = chat.client.abortSmp()
    } yield utils.Results.Success()
  }

  def receiveMessage(id: Int, message: String): FResult[Results.Success] = {
    for {
      chat <- getChat(id)

      _ = Platform.runLater {
        chat.messages += ChatMessage(id, clientState.id.value, message)
      }
    } yield utils.Results.Success()
  }

  def requestSmp(id: Int, secret: String, message: String): FResult[Results.Success] = {
    for {
      chat <- getChat(id)

      _ = chat.client.initSmp(secret.getBytes, Some(message.getBytes))
    } yield utils.Results.Success()
  }

  def sendMessage(id: Int, message: String): FResult[Results.Success] = {
    for {
      chat <- getChat(id)
      res <- chat.client.send(message.getBytes)

      _ = Platform.runLater {
        chat.messages += ChatMessage(clientState.id.value, id, message)
      }
    } yield utils.Results.Success()
  }

  protected def getChat(id: Int): FResult[ChatState] =
    chatsState.chats
      .get(id)
      .toRightDisjunction(ConnectionWithIdDoesNotExists(id))

}

object ChatService {

  case class AlreadyConnectedTo(name: String) extends Throwable("Already connected")

  case class UserDoesNotExists(name: String) extends Throwable("User does not exists")

  case class ConnectionWithIdDoesNotExists(id: Int) extends Throwable("Connection with specified id does not exists")

}
