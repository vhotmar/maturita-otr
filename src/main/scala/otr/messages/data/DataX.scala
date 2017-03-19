package otr.messages.data

import java.security.PublicKey

import otr.utils.BitVectorConversions._
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import otr.{Parameters, PartialPublic, Types}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
import utils.Results.FResult
import utils.{EParsable, EParsableCompanion}

case class DataX(
  localLongTermPublicKey: PublicKey,
  localKeyId: Int,
  signature: ByteVector
) extends EParsable {
  type E = DataX

  def companion = DataX
}

object DataX extends EParsableCompanion[DataX] {
  def codec: Codec[DataX] = {
    ("localLongTermPublicKey" | Types.publicDSAKey) ::
      ("localKeyId" | int32) ::
      ("signature" | Types.data)
  }.as[DataX]

  def create(local: otr.Pair, remote: PartialPublic, parameters: Parameters): FResult[DataX] = {
    val dataM = DataM(local, remote)

    for {
    // encode dataM, create mac and signature of these data
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
