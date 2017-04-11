package otr.handlers.ake

import java.security.KeyPair

import otr._
import otr.handlers.DataHandler
import otr.messages.types.{Encrypted, Hash}
import otr.messages.{RevealSignature, Signature}
import otr.state.DataState
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto

import scalaz.Scalaz._

case class RevealSignatureHandler(
                                   encryptedPublicKey: Encrypted,
                                   hashedPublicKey: Hash,
                                   keyPair: KeyPair,
                                   longTermKeyPair: KeyPair
                                 ) extends Handler {

  import otr.utils.Validate._

  protected def process: Process = {
    case message@RevealSignature(r, encryptedSignature, macSignature) =>
      for {
      // Check if hash from DHCommit message is correct, by decrypting their public key
        remotePublicKeyBytes <- encryptedPublicKey.decrypt(r)
        hashValid <- hashedPublicKey.verify(remotePublicKeyBytes).right[Throwable].validate("Invalid hash")

        // Parse their public key
        remotePublicKey <- Crypto.parseECKey(remotePublicKeyBytes)

        // Create temporary state
        temporaryState <- NonCompleteState.create(keyPair, remotePublicKey, longTermKeyPair)

        // Get final state + validation
        state <- message.process(temporaryState.local, temporaryState.remote, temporaryState.parameters)

        signature <- Signature.create(state)
      } yield HandlerResult(
        signature,
        DataHandler(DataState(state))
      )
  }
}
