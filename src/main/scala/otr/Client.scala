package otr

import otr.actions.{ReceiveMessageAction, SendMessageAction}
import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.messages.Empty
import otr.requests.SendMessageRequest
import otr.utils.Message
import scodec.Codec


class Client(var handler: Handler, sender: Sender, data: ClientData) extends Receiver {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  val codec: Codec[Message] = Message.codec(4, 1, 1)
  var requests: List[Request] = List.empty

  def receive(bytes: Array[Byte]): FResult[Message] = {
    val message: FResult[Message] = codec.decode(bytes)

    message.foreach(handleMessage)

    message
  }

  def initialized: Boolean = handler.canHandleRequest(SendMessageRequest(Array.empty[Byte]))

  def send(bytes: Array[Byte]): Unit = {
    val request = SendMessageRequest(bytes)

    if (handler.canHandleRequest(request))
      handler
        .handleRequest(request)
        .fold(e => throw e, handleHandlerResult)
    else
      requests = requests :+ request
  }

  def init(): Unit = {
    codec.encode(Empty()).foreach(msg => receive(msg))
  }

  protected def handleMessage(message: Message): Unit = {
    handler
      .handle(message)
      .fold(e => throw e, handleHandlerResult)
  }

  protected def handleHandlerResult(result: HandlerResult): Unit = {
    // need to keep handler synchronized - we should not need it,
    // but just in case keep it here
    // TODO: investigate where to put synchronized - probably not here
    synchronized({
      this.setHandler(result.newHandler)
    })

    result.actions.foreach({
      case SendMessageAction(message) => encodeAndSendMessage(message)
      case ReceiveMessageAction(d) => println("Received message:", new String(d))
    })
  }

  protected def encodeAndSendMessage(message: Message): Unit = {
    codec.encode(message).foreach(vec => sender.send(vec))
  }

  protected def setHandler(newHandler: Handler): Unit = {
    handler = newHandler

    val (canHandle, cantHandle) = requests.partition(handler.canHandleRequest(_))

    requests = cantHandle

    val handled = canHandle.flatMap(handler.handleRequest(_).toOption)

    handled.foreach(handleHandlerResult)
  }
}

object Client {

  def apply(handler: Handler, sender: Sender, data: ClientData): Client = new Client(handler, sender, data)

  def create(sender: Sender, data: ClientData, init: Boolean = false): FResult[Client] =
    if (init) InitHandler.create().map(handler => Client(handler, sender, data))
    else DHCommitHandler.create().map(handler => Client(handler, sender, data))
}

case class ClientData(name: String) {

}


