package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import otr.Types
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Data(remoteId: Int, message: ByteVector) extends Message {
  type E = Data

  def companion = Data
}

object Data extends MessageCompanion[Data] {
  override def command: ByteVector = hex"04"

  override def codec(config: MessageConfig): Codec[Data] = {
    ("remoteId" | int32) :: ("message" | Types.data)
  }.as[Data]
}




