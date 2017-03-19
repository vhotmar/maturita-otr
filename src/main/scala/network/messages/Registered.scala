package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Registered(name: String, id: Int) extends Message {
  type E = Registered

  def companion = Registered
}

object Registered extends MessageCompanion[Registered] {
  override def command: ByteVector = hex"03"

  override def codec(config: MessageConfig): Codec[Registered] = {
    ("name" | utf8_32) :: ("id" | int32)
  }.as[Registered]
}




