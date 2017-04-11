package otr

import _root_.utils.Results.{FResult, Success}
import otr.actions._
import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.requests._
import otr.utils.{Message, MessageConfig}
import scodec.Codec

import scalaz.Scalaz._


class Client(
              var handler: Handler,
              sender: Sender,
              clientListener: ClientListener,
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

    processByHandlerOrQueue(request)
  }

  def initSmp(secret: Array[Byte], question: Option[Array[Byte]]): FResult[Returned] = {
    val request = InitSmpRequest(secret, question)

    processByHandler(request)
  }

  def answerSmp(secret: Array[Byte]): FResult[Returned] = {
    processByHandler(AnswerSmpRequest(secret))
  }

  def abortSmp(): FResult[Returned] = {
    processByHandler(AbortSmpRequest())
  }

  def init(): Unit = {
    processByHandler(InitRequest())
  }

  protected def handleAction(action: Action): FResult[Success] = {
    action match {
      case SendMessageAction(message) => encodeAndSendMessage(message)
      case ReceiveMessageAction(d) => if (d.length > 0) clientListener.receive(d)
      case ReceiveSmpAction(message) => clientListener.smpRequestReceived(message)
      case ResultSmpAction(res) => clientListener.smpResult(res)
      case AbortSmpAction() => clientListener.smpAbort()
    }

    Success().right
  }

  protected def encodeAndSendMessage(message: Message): Unit = {
    codec.encode(message).foreach(vec => sender.send(vec))
  }
}

object Client {

  def create(sender: Sender, clientListener: ClientListener, data: ClientData, init: Boolean = false): FResult[Client] =
    if (init) InitHandler.create().map(handler => Client(handler, sender, clientListener, data))
    else DHCommitHandler.create().map(handler => Client(handler, sender, clientListener, data))

  def apply(handler: Handler, sender: Sender, clientListener: ClientListener, data: ClientData): Client =
    new Client(handler, sender, clientListener, data)
}

case class ClientData(name: String)


