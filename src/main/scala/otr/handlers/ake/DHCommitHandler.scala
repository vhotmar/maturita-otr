package otr.handlers.ake

import java.security.KeyPair

import otr.messages.{DHCommit, DHKey}
import otr.utils.Crypto
import otr.{Handler, HandlerResult}
import utils.Results.FResult

import scalaz.Scalaz._

case class DHCommitHandler(
  keyPair: KeyPair,
  longTermKeyPair: KeyPair
) extends Handler {
  protected def process: Process = {
    case DHCommit(encryptedPublicKey, hashedPublicKey) =>
      HandlerResult(
        DHKey(keyPair.getPublic),
        RevealSignatureHandler(encryptedPublicKey, hashedPublicKey, keyPair, longTermKeyPair)
      ).right[Throwable]
  }
}

object DHCommitHandler {
  // can't use apply here - because there might be errors,
  // while generating keyPairs
  def create(): FResult[DHCommitHandler] =
  for {
    keyPair <- Crypto.generateECKeyPair()
    longTermKeyPair <- Crypto.generateDSAKeyPair()
  } yield new DHCommitHandler(keyPair, longTermKeyPair)
}
