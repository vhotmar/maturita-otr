package network.messages

import network.{Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class UserDoesNotExist(name: String) extends Message {
  type E = UserDoesNotExist

  def companion = UserDoesNotExist
}

object UserDoesNotExist extends MessageCompanion[UserDoesNotExist] {
  override def command: ByteVector = hex"03"

  override def codec(config: MessageConfig): Codec[UserDoesNotExist] = {
    "name" | string32
  }.as[UserDoesNotExist]
}




