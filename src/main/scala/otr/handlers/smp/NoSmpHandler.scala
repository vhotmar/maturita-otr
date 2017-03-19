package otr.handlers.smp

import otr.Handler
import otr.actions.InitSMPAction
import otr.utils.Crypto

case class NoSmpHandler() extends Handler {
  override protected def process: Process = {
    case InitSMPAction(secret: Array[Byte], question: Option[Array[Byte]]) =>
      val secretMpi = BigInt(1, smpSecret)

      def randomExponent() = BigInt(1, Crypto.randomBytes(192))

      val x2 = randomExponent()
      val x3 = randomExponent()


  }
}



