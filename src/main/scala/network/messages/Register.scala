package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Register(name: String) extends Message {
  type E = Register

  def companion = Register
}

object Register extends MessageCompanion[Register] {
  override def command: ByteVector = hex"07"

  override def codec(config: MessageConfig): Codec[Register] = {
    "name" | utf8_32
  }.as[Register]
}




