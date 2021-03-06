package otr.messages

import java.security.PublicKey

import otr.Types
import otr.utils.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class DHKey(publicKey: PublicKey) extends Message {
  type E = DHKey

  def companion = DHKey
}

case object DHKey extends MessageCompanion[DHKey] {
  def codec(config: MessageConfig): Codec[DHKey] = {
    "publicKey" | Types.publicECKey
  }.as[DHKey]

  def command: ByteVector = hex"0x0a"
}
