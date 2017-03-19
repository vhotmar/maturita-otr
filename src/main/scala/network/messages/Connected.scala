package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Connected(remoteUserName: String, remoteId: Int) extends Message {
  type E = Connected

  def companion = Connected
}

object Connected extends MessageCompanion[Connected] {
  override def command: ByteVector = hex"02"

  override def codec(config: MessageConfig): Codec[Connected] = {
    ("remoteUserName" | string32) :: ("remoteId" | int32)
  }.as[Connected]
}


