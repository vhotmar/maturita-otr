package otr.messages.data

import java.security.PublicKey

import otr.Types
import otr.messages.types.Encrypted
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
import utils.{EParsable, EParsableCompanion}

case class DataT(
  senderKeyId: Int,
  receiverKeyId: Int,
  nextPublicKey: PublicKey,
  counter: ByteVector,
  encrypted: Encrypted
) extends EParsable {
  type E = DataT

  def companion = DataT
}

object DataT extends EParsableCompanion[DataT] {
  def codec: Codec[DataT] = {
    ("senderKeyId" | int32) ::
      ("receiver" | int32) ::
      ("nextSenderPublicKey" | Types.publicECKey) ::
      ("counter" | Types.data) ::
      ("encryptedMessage" | Types.encrypted)
  }.as[DataT]
}
