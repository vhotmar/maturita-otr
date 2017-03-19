package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class ConnectedFrom(remoteUserName: String, remoteId: Int) extends Message {
  type E = ConnectedFrom

  def companion = ConnectedFrom
}

object ConnectedFrom extends MessageCompanion[ConnectedFrom] {
  override def command: ByteVector = hex"02"

  override def codec(config: MessageConfig): Codec[ConnectedFrom] = {
    ("remoteUserName" | utf8_32) :: ("remoteId" | int32)
  }.as[ConnectedFrom]
}


