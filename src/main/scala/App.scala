import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider
import otr.{Client, FResult, Receiver, Sender}

import scalaz.Scalaz._

object App {
  def main(args: Array[String]) {
    Security.addProvider(new BouncyCastleProvider())

    println("Hello!")

    val (alice, bob) = {
      val (aSender, aReceiverManager) = MockChannelProvider.createChannel()
      val (bSender, bReceiverManager) = MockChannelProvider.createChannel()

      val alice = Client.create(bSender, init = true).toOption.get
      val bob = Client.create(aSender).toOption.get

      aReceiverManager.subscribe(alice)
      bReceiverManager.subscribe(bob)

      (alice, bob)
    }

    alice.init()
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

    def subscribe(receiver: Receiver): Unit = {
      subjects += receiver
    }

    def receive(message: Array[Byte]): FResult[String] = {
      subjects.foreach(_.receive(message))

      "ok".right[Throwable]
    }
  }

  def createChannel(): Tuple2[Sender, MockReceiverManager] = {
    val manager = new MockReceiverManager

    (new MockSender(manager), manager)
  }
}