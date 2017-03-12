package otr.handlers.ake

import java.security.KeyPair

import otr._
import otr.messages.types.{Encrypted, Hash}
import otr.messages.{RevealSignature, Signature}
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto

import scalaz.Scalaz._
import scalaz._

case class RevealSignatureHandler(encryptedPublicKey: Encrypted, hashedPublicKey: Hash, keyPair: KeyPair, longTermKeyPair: KeyPair) extends Handler {
  protected def process: Process = {
    case message@RevealSignature(r, encryptedSignature, macSignature) =>
      def invariant(predicate: => Boolean, message: String): FResult[Boolean] =
        if (!predicate) ValidationError(message).left else \/-(true)

      def invariantp(predicate: => FResult[Boolean], message: String): FResult[Boolean] =
        predicate.flatMap(b => if (b) true.right else ValidationError(message).left)


      for {
      // Check if hash from DHCommit message is correct, by decrypting their public key
        remotePublicKeyBytes <- encryptedPublicKey.decrypt(r)
        hashValid <- invariant(hashedPublicKey.verify(remotePublicKeyBytes), "Invalid hash")

        // Parse their public key
        remotePublicKey <- Crypto.parseECKey(remotePublicKeyBytes)

        // Create temporary state
        temporaryState <- NonCompleteState.create(keyPair, remotePublicKey, longTermKeyPair)

        // Get final state + validation
        state <- message.process(temporaryState.local, temporaryState.remote, temporaryState.parameters)

        signature <- Signature.create(state)
      } yield HandlerResult(
        signature,
        this
      )
  }
}
