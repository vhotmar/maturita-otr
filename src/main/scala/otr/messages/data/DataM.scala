package otr.messages.data

import java.security.PublicKey

import otr.{PartialPublic, Public, Types}
import scodec.Codec
import scodec.codecs._
import utils.{EParsable, EParsableCompanion}

case class DataM(
  localPublicKey: PublicKey,
  remotePublicKey: PublicKey,
  localLongTermPublicKey: PublicKey,
  localKeyId: Int = 1
) extends EParsable {

  type E = DataM

  def companion = DataM
}

object DataM extends EParsableCompanion[DataM] {
  def codec: Codec[DataM] = {
    ("localPublicKey" | Types.publicECKey) ::
      ("remotePublicKey" | Types.publicECKey) ::
      ("localLongTermPublicKey" | Types.publicDSAKey) ::
      ("localKeyId" | scodec.codecs.int32)
  }.as[DataM]

  def apply(state: otr.State): DataM =
    DataM(state.local, state.remote)

  def apply(local: Public, remote: PartialPublic): DataM =
    new DataM(
      local.publicKey,
      remote.publicKey,
      local.longTermPublicKey,
      local.keyId
    )
}
