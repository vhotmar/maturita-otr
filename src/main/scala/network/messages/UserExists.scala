package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class UserExists(name: String) extends Message {
  type E = UserExists

  def companion = UserExists
}

object UserExists extends MessageCompanion[UserExists] {
  override def command: ByteVector = hex"03"

  override def codec(config: MessageConfig): Codec[UserExists] = {
    "name" | utf8_32
  }.as[UserExists]
}




