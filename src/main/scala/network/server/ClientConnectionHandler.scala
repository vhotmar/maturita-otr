package network.server

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString
import network.{Message, MessageConfig}

class ClientConnectionHandler(private val connection: ActorRef, private val server: ActorRef, private val userManager: ActorRef)
  extends Actor with ActorLogging {

  import otr.utils.BitVectorConversions._

  val codec = Message.codec(MessageConfig(1))
  val client = context.actorOf(ClientHandler.props(self, userManager))

  def receive = {
    case Received(data) =>
      codec
        .decode(data.toArray)
        .toOption
        .foreach(m => client ! m.value)

    case ToWrite(message) =>
      codec
        .encode(message)
        .toOption
        .foreach(f =>
          connection ! Write(ByteString(f)))


    case PeerClosed => stop()
    case ErrorClosed => stop()
    case Closed => stop()
    case ConfirmedClosed => stop()
    case Aborted => stop()
  }

  def stop() = {
    client ! ClientHandler.End()

    context stop self
  }
}

object ClientConnectionHandler {
  def props(ref: ActorRef, self: ActorRef, userManager: ActorRef) = Props(classOf[ClientConnectionHandler], ref, self, userManager)
}
