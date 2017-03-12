package otr.messages

import otr._
import otr.messages.data.DataX
import otr.messages.types.{Encrypted, Mac}
import otr.utils.BitVectorConversions._
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Signature(encryptedSignature: Encrypted, macSignature: Mac) extends Message {
  type E = Signature

  def companion = Signature
}

object Signature extends MessageCompanion[Signature] {
  def codec(version: Int): Codec[Signature] = {
    ("encryptedSignature" | Types.encrypted) ::
      ("macSignature" | Types.mac(20))
  }.as[Signature]

  def command: ByteVector = hex"0x12"

  def create(state: State): FResult[Signature] = {
    for {
      dataX <- DataX.create(state.local, state.remote, state.parameters)
      encodedDataX <- dataX.encode
      encryptedDataX <- Crypto.encryptAES(encodedDataX, state.parameters.c)
      mac <- Mac.create(encryptedDataX, state.parameters.m2, 20)
    } yield Signature(
      Encrypted(encryptedDataX),
      mac
    )
  }
}
