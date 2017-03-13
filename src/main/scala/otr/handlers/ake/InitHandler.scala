package otr.handlers.ake

import java.security.KeyPair

import otr.messages.DHCommit
import otr.messages.types.{Encrypted, Hash}
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import otr.{FResult, Handler, HandlerResult}

case class InitHandler(
  keyPair: KeyPair,
  longTermKeyPair: KeyPair
) extends Handler {
  // get random r
  val r: Array[Byte] = Crypto.randomBytes(16)

  // encoded publicKey
  val publicKeyEncoded: Array[Byte] = keyPair.getPublic.getEncoded

  protected def process: Process = {
    case _ =>
      for {
        encrypted <- Encrypted.create(publicKeyEncoded, r)
      } yield HandlerResult(
        DHCommit(
          encrypted,
          Hash(publicKeyEncoded, calculateHash = true)
        ),
        DHKeyHandler(r, keyPair, longTermKeyPair)
      )
  }
}

object InitHandler {
  // can't use apply here - because there might be errors,
  // while generating keyPairs
  def create(): FResult[InitHandler] =
    for {
      keyPair <- Crypto.generateECKeyPair()
      longTermKeyPair <- Crypto.generateDSAKeyPair()
    } yield InitHandler(keyPair, longTermKeyPair)
}
