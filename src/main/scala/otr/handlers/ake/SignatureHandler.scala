package otr.handlers.ake

import otr.{Handler, NonCompleteState, ValidationError}

import scalaz.Scalaz._

case class SignatureHandler(state: NonCompleteState) extends Handler {
  override protected def process: Process = {
    case _ => ValidationError(":(").left
  }
}
