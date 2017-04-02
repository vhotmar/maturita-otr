import java.net.InetSocketAddress

import akka.actor.ActorSystem
import network.Server

object ServerApp extends App {
  implicit val system = ActorSystem("Server")

  val actor = system.actorOf(Server.props(new InetSocketAddress("localhost", 8765)))
}
