import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider
import otr.{Client, ClientData, Receiver, Sender}
import utils.Results.FResult

import scalaz.Scalaz._

object App {
  def main(args: Array[String]) {
    Security.addProvider(new BouncyCastleProvider())

    println("Hello!")

    val (aSender, aReceiverManager) = MockChannelProvider.createChannel()
    val (bSender, bReceiverManager) = MockChannelProvider.createChannel()

    val alice = Client.create(bSender, ClientData("alice"), init = true).toOption.get
    val bob = Client.create(aSender, ClientData("bob")).toOption.get

    aReceiverManager.subscribe(alice)
    bReceiverManager.subscribe(bob)


    alice.init()

    bob.send("Im bob".getBytes)
    bob.send("I write".getBytes)
    bob.send("Trolololol".getBytes)
    alice.send("Heyy, thats cool".getBytes)
    bob.send("I know".getBytes)
    bob.send("You are not".getBytes)
    alice.send("Heyy man".getBytes)
    alice.send("How could you say that to me :(".getBytes)


    println("Check sending messages with same texts provides different results")
    bReceiverManager.enableLogging
    alice.send("nice".getBytes)
    alice.send("nice".getBytes)
    alice.send("nice".getBytes)
    bReceiverManager.disableLogging
  }
}

object MockChannelProvider {

  class MockSender(receiver: Receiver) extends Sender {
    override def send(message: Array[Byte]): Unit = {
      receiver.receive(message)
    }
  }

  class MockReceiverManager extends Receiver {
    private var subjects: Set[Receiver] = Set()
    private var enabledLogging = false

    def subscribe(receiver: Receiver): Unit = {
      subjects += receiver
    }

    def receive(message: Array[Byte]): FResult[String] = {
      import otr.utils.ByteVectorConversions._

      if (enabledLogging) println(message.toHex)

      subjects.foreach(_.receive(message))

      "ok".right[Throwable]
    }

    def enableLogging: Unit = enabledLogging = true

    def disableLogging: Unit = enabledLogging = false
  }

  def createChannel(): (MockSender, MockReceiverManager) = {
    val manager = new MockReceiverManager

    (new MockSender(manager), manager)
  }
}