package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import network.Client._

class ClientHandler(client: ActorRef, listenerActor: ActorRef) {
  client ! Client.AddListener(listenerActor)

  def connect(address: InetSocketAddress) = client ! Client.Connect(address)

  def register(name: String) = client ! Client.Register(name)

  def connectTo(name: String) = client ! Client.ConnectTo(name)

  def sendMessage(id: Int, message: Array[Byte]) = client ! Client.SendMessage(id, message)

  def disconnect(id: Int) = client ! Client.Disconnect(id)
}

object ClientHandler {
  def apply(client: ActorRef)(implicit actorSystem: ActorSystem) =
    new ClientHandler(client, actorSystem.actorOf(Props[ClientListenerActor]))

  class ClientListenerActor(listener: ClientListener) extends Actor {
    def receive = {
      case Connect(addr) => listener.connecting(addr)
      case Connected() => listener.connected()
      case ConnectionFailed() => listener.connectionFailed()
      case Registered(name, id) => listener.registered(name, id)
      case NameAlreadyRegistered(name) => listener.nameTaken(name)
      case ConnectTo(name) => listener.connectingTo(name)
      case SendMessage(id, message) => listener.sendingMessage(id, message)
      case Disconnect(id) => listener.disconnect()
      case Disconnected(id) => listener.disconnected()
      case UserDoesNotExists(name) => listener.userDoesNotExists(name)
      case ConnectedToUser(name, id) => listener.connectedToUser(name, id)
      case ConnectionFromUser(name, id) => listener.connectionFromUser(name, id)
      case ConnectionClosed() => listener.connectionClosed()
      case ReceivedData(id, message) => listener.receivedMessage(id, message)
    }
  }

}