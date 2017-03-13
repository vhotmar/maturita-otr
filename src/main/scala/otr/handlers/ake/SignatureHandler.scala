package otr.handlers.ake

import otr.handlers.DataHandler
import otr.messages.Signature
import otr.state.DataState
import otr.{Handler, HandlerResult, NonCompleteState}

case class SignatureHandler(state: NonCompleteState) extends Handler {
  override protected def process: Process = {
    case message@Signature(_, _) =>
      // just process signature message (includes validation and state creation)
      message
        .process(state.local, state.remote, state.parameters)
        .map(state => HandlerResult(List(), DataHandler(DataState(state))))
  }
}
