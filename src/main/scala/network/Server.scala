package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import network.server.{ClientConnectionHandler, UserManager}

class Server(address: InetSocketAddress) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, address)
  log.debug(s"Binding: $address")

  private val userManager: ActorRef = context.actorOf(Props[UserManager])

  def receive = {

    case b@Bound(localAddress) =>
      log.info(s"Successfully $b")

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      val handler = context.actorOf(ClientConnectionHandler.props(sender(), self, userManager))
      sender ! Register(handler)

  }
}

object Server {
  def props(address: InetSocketAddress): Props = {
    Props(classOf[Server], address)
  }
}
