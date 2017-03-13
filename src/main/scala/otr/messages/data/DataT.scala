package otr.messages.data

import java.security.PublicKey

import otr.Types
import otr.messages.types.Encrypted
import otr.utils.{Parsable, ParsableCompanion}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

case class DataT(
  senderKeyId: Int,
  receiverKeyId: Int,
  nextPublicKey: PublicKey,
  counter: ByteVector,
  encrypted: Encrypted
) extends Parsable {
  type E = DataT

  def companion = DataT
}

object DataT extends ParsableCompanion[DataT] {
  def codec: Codec[DataT] = {
    ("senderKeyId" | int32) ::
      ("receiver" | int32) ::
      ("nextSenderPublicKey" | Types.publicECKey) ::
      ("counter" | Types.data) ::
      ("encryptedMessage" | Types.encrypted)
  }.as[DataT]
}
