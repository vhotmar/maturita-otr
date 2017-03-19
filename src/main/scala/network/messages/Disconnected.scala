package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Disconnected(remoteId: Int) extends Message {
  type E = Disconnected

  def companion = Disconnected
}

object Disconnected extends MessageCompanion[Disconnected] {
  override def command: ByteVector = hex"01"

  override def codec(config: MessageConfig): Codec[Disconnected] = {
    "remoteId" | int32
  }.as[Disconnected]
}


