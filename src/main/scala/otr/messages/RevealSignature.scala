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

import scalaz.Scalaz._

trait SignatureData {
  val encryptedSignature: Encrypted
  val macSignature: Mac

  def process(local: Local, remote: NonCompleteRemote, parameters: Parameters): FResult[otr.State] = {
    for {
    // Validate this message
      validMac <- valid(parameters.m2)

      // Decrypt and parse data
      decryptedSignature <- encryptedSignature.decrypt(parameters.c)
      dataX <- DataX.decode(decryptedSignature)

      state = otr.State(local, Remote(remote.publicKey, dataX.localLongTermPublicKey, dataX.localKeyId), parameters)

      // validate signature
      signatureValid <- Crypto.verify(dataX.signature, state.remote.longTermPublicKey)
    } yield state
  }

  def valid(mac: Array[Byte]): FResult[Boolean] =
    Crypto.verifyMac(mac, encryptedSignature.bytes, macSignature.bytes)
      .flatMap(b => if (b) true.right else ValidationError("Invalid signature data").left)
}

case class RevealSignature(
  revealedKey: ByteVector,
  encryptedSignature: Encrypted,
  macSignature: Mac
) extends Message with SignatureData {
  type E = RevealSignature

  def companion = RevealSignature
}

object RevealSignature extends MessageCompanion[RevealSignature] {
  def codec(version: Int): Codec[RevealSignature] = {
    ("revealedKey" | Types.data) ::
      ("encryptedSignature" | Types.encrypted) ::
      ("macSignature" | Types.mac(20))
  }.as[RevealSignature]

  def command: ByteVector = hex"0x11"

  def create(r: Array[Byte], state: NonCompleteState): FResult[RevealSignature] = {
    for {
      dataX <- DataX.create(state.local, state.remote, state.parameters)
      encodedDataX <- dataX.encode
      encryptedDataX <- Crypto.encryptAES(encodedDataX, state.parameters.c)
      mac <- Mac.create(encryptedDataX, state.parameters.m2, 20)
    } yield RevealSignature(
      r,
      Encrypted(encryptedDataX),
      mac
    )
  }
}

