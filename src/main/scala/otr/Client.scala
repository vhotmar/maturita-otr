package otr

import _root_.utils.Results.{FResult, Success}
import otr.actions.{InitAction, ReceiveMessageAction, SendMessageAction}
import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.requests.SendMessageRequest
import otr.utils.{Message, MessageConfig}
import scodec.Codec

import scalaz.Scalaz._


class Client(
              var handler: Handler,
              sender: Sender,
              receiver: Receiver,
              data: ClientData
) extends Receiver with HandlerManager {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  val codec: Codec[Message] = Message.codec(MessageConfig(4, 1, 1))

  def receive(bytes: Array[Byte]): FResult[Message] = {
    val message: FResult[Message] = codec.decode(bytes)

    message
      .flatMap(msg => processByHandler(msg).map(_ => msg))
  }

  def initialized: Boolean = handler.canHandle(SendMessageRequest(Array.empty[Byte]))

  def send(bytes: Array[Byte]): FResult[Returned] = {
    val request = SendMessageRequest(bytes)

    if (handler.canHandle(request))
      processByHandler(request)
    else {
      queue(request)

      RQueued().right
    }
  }

  def init(): Unit = {
    processByHandler(InitAction())
  }

  protected def handleAction(action: Action): FResult[Success] = {
    action match {
      case SendMessageAction(message) => encodeAndSendMessage(message)
      case ReceiveMessageAction(d) => receiver.receive(d)
    }

    Success().right
  }

  protected def encodeAndSendMessage(message: Message): Unit = {
    codec.encode(message).foreach(vec => sender.send(vec))
  }
}

object Client {

  def create(sender: Sender, receiver: Receiver, data: ClientData, init: Boolean = false): FResult[Client] =
    if (init) InitHandler.create().map(handler => Client(handler, sender, receiver, data))
    else DHCommitHandler.create().map(handler => Client(handler, sender, receiver, data))

  def apply(handler: Handler, sender: Sender, receiver: Receiver, data: ClientData): Client = new Client(handler, sender, receiver, data)
}

case class ClientData(name: String)


