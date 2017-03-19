package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Disconnect(remoteId: Int) extends Message {
  type E = Disconnect

  def companion = Disconnect
}

object Disconnect extends MessageCompanion[Disconnect] {
  override def command: ByteVector = hex"03"

  override def codec(config: MessageConfig): Codec[Disconnect] = {
    "remoteId" | int32
  }.as[Disconnect]
}




