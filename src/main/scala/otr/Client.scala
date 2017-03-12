package otr

import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.messages.Empty
import scodec.Codec

class Client(var handler: Handler, sender: Sender) extends Receiver {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  val codec: Codec[Message] = Message.codec(4, 1, 1)

  def receive(bytes: Array[Byte]): FResult[Message] = {
    println(s"size - ${bytes.length}")
    val message: FResult[Message] = codec.decode(bytes)

    message.foreach(handleMessage)

    message
  }

  def init(): Unit = {
    codec.encode(Empty()).foreach(msg => receive(msg))
  }

  protected def handleMessage(message: Message): Unit = {
    println("--------------------------------")
    print("Incoming: ")
    println(message.getClass)
    print("Handler: ")
    println(handler.getClass)

    val handled = handler
      .handle(message)

    handled.swap.foreach(err => println("Error", err))

    handled
      .foreach(result => {
        print("New handler: ")
        println(result.newHandler.getClass)

        // Need to keep handler synchronized - we should not need it,
        // but just in case keep it here
        synchronized({
          handler = result.newHandler
        })

        result.messages.foreach(sendMessage)
      })
  }

  protected def sendMessage(message: Message): Unit = {
    print("Send message: ")
    println(message.getClass)
    codec.encode(message).foreach(vec => sender.send(vec))
  }
}

object Client {

  def apply(handler: Handler, sender: Sender): Client = new Client(handler, sender)

  def create(sender: Sender, init: Boolean = false): FResult[Client] =
    if (init) InitHandler.create().map(handler => Client(handler, sender))
    else DHCommitHandler.create().map(handler => Client(handler, sender))
}


