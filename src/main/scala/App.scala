import java.security.Security

import _root_.utils.Results.FResult
import org.bouncycastle.jce.provider.BouncyCastleProvider
import otr._

import scalaz.Scalaz._

object App {
  def main(args: Array[String]) {
    Security.addProvider(new BouncyCastleProvider())

    println("Hello!")

    val (aSender, aReceiverManager) = MockChannelProvider.createChannel()
    val (bSender, bReceiverManager) = MockChannelProvider.createChannel()

    val rec = (name: String) => new ClientListener {
      override def smpRequestReceived(question: Option[String]): Unit = {
        println(name, "SMP Request received", question)
      }

      override def receive(bytes: Array[Byte]): FResult[Any] = {
        println(name, "Received", new String(bytes))

        1.right
      }

      override def smpResult(res: Boolean): Unit = {
        println(name, "SMP Result")
      }

      override def smpAbort(): Unit = {
        println(name, "SMP Abort")
      }
    }

    val alice = Client.create(bSender, rec("alice"), ClientData("alice"), init = true).toOption.get
    val bob = Client.create(aSender, rec("bob"), ClientData("bob")).toOption.get

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
    bReceiverManager.enableLogging()
    alice.send("nice".getBytes)
    alice.send("nice".getBytes)
    alice.send("nice".getBytes)
    bReceiverManager.disableLogging()

    println("SMP")

    alice.initSmp("asdf".getBytes, None)
    bob.answerSmp("asdfss".getBytes)

    println("END")
  }
}

object MockChannelProvider {

  def createChannel(): (MockSender, MockReceiverManager) = {
    val manager = new MockReceiverManager

    (new MockSender(manager), manager)
  }

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

    def enableLogging(): Unit = enabledLogging = true

    def disableLogging(): Unit = enabledLogging = false
  }
}