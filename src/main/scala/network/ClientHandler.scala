package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import network.Client._

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

class ClientHandler(client: ActorRef, listenerActor: ActorRef) {

  import scala.concurrent.ExecutionContext.Implicits.global

  client ! Client.AddListener(listenerActor)

  def addListener[T](listenerCreator: ((T) => Unit, (Throwable) => Unit) => ClientListener): Future[T] = {
    val promise = Promise[T]()

    val listener = listenerCreator(promise.success, promise.failure)

    addListener(listener)

    val future = promise.future

    future.onComplete(x => removeListener(listener))

    future
  }

  def addListener(listener: ClientListener): Unit = listenerActor ! ClientHandler.AddListener(listener)

  def removeListener(listener: ClientListener): Unit = listenerActor ! ClientHandler.RemoveListener(listener)

  def connect(address: InetSocketAddress): Unit = client ! Client.Connect(address)

  def register(name: String): Unit = client ! Client.Register(name)

  def connectTo(name: String): Unit = client ! Client.ConnectTo(name)

  def sendMessage(id: Int, message: Array[Byte]): Unit = client ! Client.SendMessage(id, message)

  def disconnect(id: Int): Unit = client ! Client.Disconnect(id)
}

object ClientHandler {
  def apply(client: ActorRef)(implicit actorSystem: ActorSystem) =
    new ClientHandler(client, actorSystem.actorOf(Props[ClientListenerActor]))

  case class AddListener(listener: ClientListener)

  case class RemoveListener(listener: ClientListener)

  class ClientListenerActor() extends Actor with ActorLogging {
    val listeners: mutable.Set[ClientListener] = mutable.Set.empty

    def receive = {
      case ClientHandler.AddListener(c) =>
        listeners += c

      case ClientHandler.RemoveListener(c) =>
        listeners -= c

      case Connect(addr) =>
        listeners.foreach(_.connecting(addr))

      case Connected() =>
        listeners.foreach(_.connected())

      case ConnectionFailed() => listeners.foreach(_.connectionFailed())
      case Register(name) => listeners.foreach(_.registering(name))
      case Registered(name, id) => listeners.foreach(_.registered(name, id))
      case NameAlreadyRegistered(name) => listeners.foreach(_.nameTaken(name))
      case ConnectTo(name) => listeners.foreach(_.connectingTo(name))
      case SendMessage(id, message) => listeners.foreach(_.sendingMessage(id, message))
      case Disconnect(id) => listeners.foreach(_.disconnect())
      case Disconnected(id) => listeners.foreach(_.disconnected(id))
      case UserDoesNotExists(name) => listeners.foreach(_.userDoesNotExists(name))
      case ConnectedToUser(name, id) => listeners.foreach(_.connectedToUser(name, id))
      case ConnectionFromUser(name, id) => listeners.foreach(_.connectionFromUser(name, id))
      case ConnectionClosed() => listeners.foreach(_.connectionClosed())
      case ReceivedData(id, message) => listeners.foreach(_.receivedMessage(id, message))
    }
  }

}