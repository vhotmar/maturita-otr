package otr.messages.data

import java.security.PublicKey

import otr._
import scodec.Codec
import scodec.codecs._

case class DataM(
  localPublicKey: PublicKey,
  remotePublicKey: PublicKey,
  localLongTermPublicKey: PublicKey,
  localKeyId: Int = 1
) extends Parsable {

  type E = DataM

  def companion = DataM
}

object DataM extends ParsableCompanion[DataM] {
  def codec: Codec[DataM] = {
    ("localPublicKey" | Types.publicECKey) ::
      ("remotePublicKey" | Types.publicECKey) ::
      ("localLongTermPublicKey" | Types.publicDSAKey) ::
      ("localKeyId" | scodec.codecs.int32)
  }.as[DataM]

  def apply(local: Public, remote: PartialPublic): DataM =
    new DataM(
      local.publicKey,
      remote.publicKey,
      local.longTermPublicKey,
      local.keyId
    )

  def apply(state: otr.State): DataM =
    DataM(state.local, state.remote)
}
