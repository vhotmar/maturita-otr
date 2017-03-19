package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class ConnectTo(name: String) extends Message {
  type E = ConnectTo

  def companion = ConnectTo
}

object ConnectTo extends MessageCompanion[ConnectTo] {
  override def command: ByteVector = hex"03"

  override def codec(config: MessageConfig): Codec[ConnectTo] = {
    "name" | utf8_32
  }.as[ConnectTo]
}



