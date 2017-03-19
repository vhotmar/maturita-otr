package otr.handlers.ake

import java.security.KeyPair

import otr.messages.{DHKey, RevealSignature}
import otr.{Handler, HandlerResult, NonCompleteState}

case class DHKeyHandler(
  r: Array[Byte],
  keyPair: KeyPair,
  longTermKeyPair: KeyPair
) extends Handler {
  protected def process: Process = {
    case DHKey(theirPublicKey) =>
      // create state and send reveal signature message
      for {
        state <- NonCompleteState.create(keyPair, theirPublicKey, longTermKeyPair)
        message <- RevealSignature.create(r, state)
      } yield HandlerResult(message, SignatureHandler(state))
  }
}
