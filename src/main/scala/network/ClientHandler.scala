package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import network.Client._

import scala.collection.mutable

class ClientHandler(client: ActorRef, listenerActor: ActorRef) {
  client ! Client.AddListener(listenerActor)

  def addListener(listener: ClientListener) = listenerActor ! ClientHandler.AddListener(listener)

  def removeListener(listener: ClientListener) = listenerActor ! ClientHandler.RemoveListener(listener)

  def connect(address: InetSocketAddress) = client ! Client.Connect(address)

  def register(name: String) = client ! Client.Register(name)

  def connectTo(name: String) = client ! Client.ConnectTo(name)

  def sendMessage(id: Int, message: Array[Byte]) = client ! Client.SendMessage(id, message)

  def disconnect(id: Int) = client ! Client.Disconnect(id)
}

object ClientHandler {
  def apply(client: ActorRef)(implicit actorSystem: ActorSystem) =
    new ClientHandler(client, actorSystem.actorOf(Props[ClientListenerActor]))

  case class AddListener(listener: ClientListener)

  case class RemoveListener(listener: ClientListener)

  class ClientListenerActor() extends Actor {
    def receive = {
      case ClientHandler.AddListener(c) =>
        listeners += c

      case ClientHandler.RemoveListener(c) =>
        listeners -= c

      case Connect(addr) => listeners.foreach(_.connecting(addr))
      case Connected() => listeners.foreach(_.connected())
      case ConnectionFailed() => listeners.foreach(_.connectionFailed())
      case Registered(name, id) => listeners.foreach(_.registered(name, id))
      case NameAlreadyRegistered(name) => listeners.foreach(_.nameTaken(name))
      case ConnectTo(name) => listeners.foreach(_.connectingTo(name))
      case SendMessage(id, message) => listeners.foreach(_.sendingMessage(id, message))
      case Disconnect(id) => listeners.foreach(_.disconnect())
      case Disconnected(id) => listeners.foreach(_.disconnected())
      case UserDoesNotExists(name) => listeners.foreach(_.userDoesNotExists(name))
      case ConnectedToUser(name, id) => listeners.foreach(_.connectedToUser(name, id))
      case ConnectionFromUser(name, id) => listeners.foreach(_.connectionFromUser(name, id))
      case ConnectionClosed() => listeners.foreach(_.connectionClosed())
      case ReceivedData(id, message) => listeners.foreach(_.receivedMessage(id, message))
    }

    def listeners: mutable.Set[ClientListener] = mutable.Set.empty
  }

}