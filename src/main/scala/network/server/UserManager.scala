package network.server

import akka.actor.{Actor, ActorRef}
import network.messages.{ConnectTo, Register}

import scala.collection.mutable

class UserManager extends Actor {
  private val users: mutable.Map[String, (Int, ActorRef)] = mutable.Map.empty
  private var latestId: Int = 1

  def receive = {
    case Register(name) =>
      if (users.contains(name))
        sender() ! UserManager.UserExists(name)
      else {
        users += (name -> (latestId -> sender()))

        sender() ! UserManager.Registered(latestId)

        latestId += 1
      }

    case ConnectTo(name) =>
      if (!users.contains(name))
        sender() ! UserManager.UserDoesNotExists(name)
      else {
        val (id, connection) = users(name)

        sender() ! UserManager.Connected(name, id, connection)
      }

    case UserManager.Close(name, id) =>
      if (users.contains(name) && users(name)._1 == id) {
        users -= name
      }
  }

}

object UserManager {

  case class Registered(id: Int)

  case class UserExists(name: String)

  case class UserDoesNotExists(name: String)

  case class Connected(name: String, id: Int, connection: ActorRef)

  case class Close(name: String, id: Int)

}
