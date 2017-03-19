package network.server

import akka.actor.{Actor, ActorRef, Props}
import network.messages._
import scodec.bits.ByteVector

import scala.collection.mutable

class ClientHandler(private val client: ActorRef, private val userManager: ActorRef) extends Actor {
  private val connections: mutable.Map[Int, ActorRef] = mutable.Map.empty

  def receive = anonymous

  def anonymous: Actor.Receive = {
    case m@Register(name) =>
      userManager ! m

      context become registrating(name)
  }

  def registrating(name: String): Actor.Receive = {
    case UserManager.Registered(id) =>
      client ! ToWrite(Registered(name, id))

      context become registered(name, id)

    case UserManager.UserExists(_) =>
      client ! ToWrite(UserExists(name))

      context become anonymous
  }

  def registered(name: String, id: Int): Actor.Receive = {
    case m@ConnectTo(remoteName) =>
      // check if user is not trying to connect to himself
      if (remoteName != name)
        userManager ! m

    case UserManager.UserDoesNotExists(remoteName) =>
      client ! ToWrite(UserDoesNotExist(remoteName))

    case UserManager.Connected(remoteName, remoteId, connection) =>
      // 1) add to current connections
      // 2) send client that connection was succesfull
      // 3) tell the other ClientHandler, that they are connected now
      connections += (remoteId -> connection)

      client ! ToWrite(Connected(remoteName, remoteId))
      connection ! ClientHandler.Connected(remoteName, remoteId, self)

    case m@Data(remoteId, message) =>
      // if we have connection with this id, send the remote ClientHandler
      // our id and the message
      if (connections.isDefinedAt(remoteId))
        connections(remoteId) ! ClientHandler.Data(id, message)

    case m@Disconnect(remoteId) =>
      // 1) remove from our connection
      // 2) notify the other ClientHandler
      // 3) send message to client about disconnect
      if (connections.contains(remoteId)) {

        connections -= remoteId

        connections(remoteId) ! ClientHandler.Disconnected(remoteId)

        client ! ToWrite(Disconnected(remoteId))
      }

    case ClientHandler.Disconnected(remoteId) =>
      // internal system send us, that we need to disconnect our client
      if (connections.contains(remoteId)) {
        connections -= remoteId

        client ! ToWrite(Disconnected(remoteId))
      }

    case ClientHandler.Data(remoteId, message) =>
      // internal system send us some data
      client ! ToWrite(Data(remoteId, message))

    case ClientHandler.Connected(remoteName, remoteId, connection) =>
      // internal system notified us, that we have new connection
      connections += (remoteId -> connection)

      client ! ToWrite(ConnectedFrom(remoteName, remoteId))

    case ClientHandler.End() =>
      // if connection ended
      // 1) notify user manager
      // 2) notify all connected conversations
      userManager ! UserManager.Close(name, id)

      connections.foreach(x => x._2 ! ClientHandler.Disconnected(id))
  }
}

object ClientHandler {

  case class End()

  case class Connected(remoteName: String, remoteId: Int, clientHandler: ActorRef)

  case class Disconnected(remoteId: Int)

  case class Data(id: Int, message: ByteVector)

  def props(client: ActorRef, userManager: ActorRef) = Props(classOf[ClientHandler], client, userManager)
}
