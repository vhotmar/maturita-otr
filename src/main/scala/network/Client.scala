package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import network.messages._
import network.server.ToWrite
import scodec.Codec

import scala.collection.mutable

class Client() extends Actor with ActorLogging {

  import context.system
  import otr.utils.BitVectorConversions._
  import otr.utils.ByteVectorConversions._

  val codec: Codec[Message] = Message.codec(MessageConfig(1))

  val listener: ActorRef = context.actorOf(Props[Client.Broadcaster])

  def receiveListener: Actor.Receive = {
    case Client.AddListener(r) =>
      listener ! Client.Broadcaster.Add(r)

    case Client.RemoveListener(r) =>
      listener ! Client.Broadcaster.Remove(r)
  }

  def receive: Actor.Receive = receiveListener orElse {
    case c@Client.Connect(addr) =>
      log.debug(s"Client connecting to $addr")
      IO(Tcp) ! Connect(addr)

      listener ! c

      context become connecting
  }

  def connecting: Actor.Receive = receiveListener orElse {
    case CommandFailed(_) =>
      log.debug(s"Connection failed")
      listener ! Client.ConnectionFailed()

      context become receive

    case c@Tcp.Connected(remote, local) =>
      log.debug(s"Successfully connected")
      listener ! Client.Connected()

      sender ! Tcp.Register(self)

      context become connected(sender)
  }

  def receiveMessage(connection: ActorRef): Actor.Receive = receiveListener orElse {
    case ToWrite(message) =>
      log.debug(s"Client sending $message")
      codec
        .encode(message)
        .toOption
        .foreach(f =>
          connection ! Write(ByteString(f: Array[Byte])))

    case Received(data) =>
      log.debug(s"Client received $data")
      codec
        .decode(data.toArray)
        .toOption
        .foreach(m => {
          log.debug(s"Client parsed received data ${m.value}")
          self ! m.value
        })
  }


  def connected(connection: ActorRef): Actor.Receive = receiveMessage(connection) orElse {
    case m@Client.Register(name) =>
      self ! ToWrite(messages.Register(name))

      listener ! m

      context become registering(connection, name)

  }


  def registering(connection: ActorRef, name: String): Actor.Receive = receiveMessage(connection) orElse {
    case Registered(name, id) =>
      listener ! Client.Registered(name, id)

      context become registered(connection, name, id)

    case UserExists(name) =>
      log.debug("User already exists")
      listener ! Client.NameAlreadyRegistered(name)

      context become connected(connection)

    case e => log.debug(s"$e")
  }

  def registered(connection: ActorRef, name: String, id: Int): Actor.Receive = receiveMessage(connection) orElse {
    case m@Client.ConnectTo(remoteName) =>
      self ! ToWrite(ConnectTo(remoteName))

      listener ! m

    case m@Client.SendMessage(id, message) =>
      self ! ToWrite(Data(id, message))

      listener ! m

    case m@Client.Disconnect(id) =>
      self ! ToWrite(Disconnect(id))

      listener ! m

    case Data(id, message) =>
      listener ! Client.ReceivedData(id, message)

    case UserDoesNotExist(name) =>
      listener ! Client.UserDoesNotExists(name)

    case messages.Connected(name, id) =>
      listener ! Client.ConnectedToUser(name, id)

    case ConnectedFrom(name, id) =>
      listener ! Client.ConnectionFromUser(name, id)

    case Disconnected(id) =>
      listener ! Client.Disconnected(id)

    case m: ConnectionClosed =>
      listener ! Client.ConnectionClosed()
  }
}

object Client {

  case class Connect(addr: InetSocketAddress)

  case class Connected()

  case class ConnectionFailed()

  case class Register(name: String)

  case class Registered(name: String, id: Int)

  case class NameAlreadyRegistered(name: String)

  case class ConnectTo(name: String)

  case class SendMessage(id: Int, message: Array[Byte])

  case class Disconnect(id: Int)

  case class ReceivedData(id: Int, message: Array[Byte])

  case class UserDoesNotExists(name: String)

  case class ConnectedToUser(name: String, id: Int)

  case class ConnectionFromUser(name: String, id: Int)

  case class Disconnected(id: Int)

  case class ConnectionClosed()

  case class AddListener(r: ActorRef)

  case class RemoveListener(r: ActorRef)

  class Broadcaster extends Actor with ActorLogging {
    val listeners: mutable.Set[ActorRef] = mutable.Set.empty

    def receive = {
      case Broadcaster.Add(ref) =>
        listeners += ref

      case Broadcaster.Remove(ref) =>
        listeners -= ref

      case m =>
        log.debug(s"Broadcast - $m")
        listeners.foreach(_ ! m)
    }
  }

  object Broadcaster {

    case class Add(a: ActorRef)

    case class Remove(a: ActorRef)

  }

}
