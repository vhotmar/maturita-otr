package otr.messages

import otr._
import otr.messages.types.{Encrypted, Hash}
import otr.utils.{Message, MessageCompanion}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class DHCommit(encryptedPublicKey: Encrypted, hashedPublicKey: Hash) extends Message {
  type E = DHCommit

  def companion = DHCommit
}

case object DHCommit extends MessageCompanion[DHCommit] {
  def codec(version: Int): Codec[DHCommit] = {
    ("encryptedPublicKey" | Types.encrypted) ::
      ("hashedPublicKey" | Types.hash)
  }.as[DHCommit]

  def command: ByteVector = hex"0x02"
}
