package otr.messages

import otr.messages.data.DataX
import otr.messages.types.{Encrypted, Mac}
import otr.utils.BitVectorConversions._
import otr.utils.ByteVectorConversions._
import otr.utils.{Crypto, Message, MessageCompanion, MessageConfig}
import otr.{State, Types}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._
import utils.Results.FResult

case class Signature(
                      encryptedSignature: Encrypted,
                      macSignature: Mac
                    ) extends Message with SignatureData {
  type E = Signature

  def companion = Signature
}

object Signature extends MessageCompanion[Signature] {
  def codec(config: MessageConfig): Codec[Signature] = {
    ("encryptedSignature" | Types.encrypted) ::
      ("macSignature" | Types.mac(20))
  }.as[Signature]

  def command: ByteVector = hex"0x12"

  def create(state: State): FResult[Signature] = {
    for {
    // create dataX
      dataX <- DataX.create(state.local, state.remote, state.parameters)
      encodedDataX <- dataX.encode

      // encrypt dataX using c
      encryptedDataX <- Crypto.encryptAES(encodedDataX, state.parameters.c)

      // sign encrypted dataX using m2
      mac <- Mac.create(encryptedDataX, state.parameters.m2, 20)
    } yield Signature(
      Encrypted(encryptedDataX),
      mac
    )
  }
}
