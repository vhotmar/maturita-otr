package otr.messages.data

import java.security.PublicKey

import otr._
import otr.utils.BitVectorConversions._
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

case class DataX(
  localLongTermPublicKey: PublicKey,
  localKeyId: Int,
  signature: ByteVector
) extends Parsable {
  type E = DataX

  def companion = DataX
}

object DataX extends ParsableCompanion[DataX] {
  def codec: Codec[DataX] = {
    ("localLongTermPublicKey" | Types.publicDSAKey) ::
      ("localKeyId" | int32) ::
      ("signature" | Types.data)
  }.as[DataX]

  def create(local: Pair, remote: PartialPublic, parameters: Parameters): FResult[DataX] = {
    val dataM = DataM(local, remote)

    for {
      encodedDataM <- dataM.encode
      hmac <- Crypto.hmac(encodedDataM, parameters.m1)
      signature <- Crypto.sign(
        hmac,
        local.longTermPrivateKey
      )
      dataX = DataX(local.longTermPublicKey, local.keyId, signature)
    } yield dataX
  }
}
