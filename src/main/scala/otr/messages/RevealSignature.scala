package otr.messages

import _root_.utils.Results.FResult
import otr._
import otr.messages.data.DataX
import otr.messages.types.{Encrypted, Mac}
import otr.utils.ByteVectorConversions._
import otr.utils.{Crypto, Message, MessageCompanion, MessageConfig}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

trait SignatureData {

  import otr.utils.Validate._

  val encryptedSignature: Encrypted
  val macSignature: Mac

  def process(local: Local, remote: NonCompleteRemote, parameters: Parameters): FResult[otr.State] = {
    for {
    // validate this message
      validMac <- validate(parameters.m2)

      // decrypt and parse data
      decryptedSignature <- encryptedSignature.decrypt(parameters.c)
      dataX <- DataX.decode(decryptedSignature)

      state = otr.State(local, Remote(remote.publicKey, dataX.localLongTermPublicKey, dataX.localKeyId), parameters)

      // validate signature
      signatureValid <- Crypto.verify(dataX.signature, state.remote.longTermPublicKey)
    } yield state
  }

  def validate(mac: Array[Byte]): FResult[Boolean] =
    macSignature
      .verify(encryptedSignature.bytes, mac)
      .validate("Invalid signature data")
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

  import otr.utils.BitVectorConversions._

  def codec(config: MessageConfig): Codec[RevealSignature] = {
    ("revealedKey" | Types.data) ::
      ("encryptedSignature" | Types.encrypted) ::
      ("macSignature" | Types.mac(20))
  }.as[RevealSignature]

  def command: ByteVector = hex"0x11"

  def create(r: Array[Byte], state: NonCompleteState): FResult[RevealSignature] = {
    for {
    // create and encode dataX
      dataX <- DataX.create(state.local, state.remote, state.parameters)
      encodedDataX <- dataX.encode

      // encrypt it using c
      encryptedDataX <- Crypto.encryptAES(encodedDataX, state.parameters.c)

      // sign it using m2
      mac <- Mac.create(encryptedDataX, state.parameters.m2, 20)
    } yield RevealSignature(
      r,
      Encrypted(encryptedDataX),
      mac
    )
  }
}

