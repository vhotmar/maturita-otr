package otr

import _root_.utils.Results.FResult
import otr.actions.{InitAction, ReceiveMessageAction, SendMessageAction}
import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.requests.SendMessageRequest
import otr.utils.Message
import scodec.Codec

import scalaz.Scalaz._
import scalaz._


class Client(
  var handler: Handler,
  sender: Sender,
  data: ClientData
) extends Receiver with HandlerManager {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  val codec: Codec[Message] = Message.codec(4, 1, 1)

  def receive(bytes: Array[Byte]): FResult[Message] = {
    val message: FResult[Message] = codec.decode(bytes)

    message
      .flatMap(msg => processByHandler(msg).map(_ => msg))
  }

  def initialized: Boolean = handler.canHandle(SendMessageRequest(Array.empty[Byte]))

  def send(bytes: Array[Byte]): FResult[Boolean] = {
    val request = SendMessageRequest(bytes)

    if (handler.canHandle(request))
      processByHandler(request)
    else {
      queue(request)

      true.right
    }
  }

  def init(): Unit = {
    processByHandler(InitAction())
  }

  protected def encodeAndSendMessage(message: Message): Unit = {
    codec.encode(message).foreach(vec => sender.send(vec))
  }

  protected def handleAction(action: Action): FResult[Boolean] = {
    action match {
      case SendMessageAction(message) => encodeAndSendMessage(message)
      case ReceiveMessageAction(d) => println("Received message:", new String(d))
    }

    true.right
  }
}

object Client {

  def apply(handler: Handler, sender: Sender, data: ClientData): Client = new Client(handler, sender, data)

  def create(sender: Sender, data: ClientData, init: Boolean = false): FResult[Client] =
    if (init) InitHandler.create().map(handler => Client(handler, sender, data))
    else DHCommitHandler.create().map(handler => Client(handler, sender, data))
}

case class ClientData(name: String)


