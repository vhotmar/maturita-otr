package otr.handlers

import otr.actions.ReceiveMessageAction
import otr.messages.Data
import otr.requests.SendMessageRequest
import otr.state.DataState
import otr.{Handler, HandlerResult}

case class DataHandler(state: DataState) extends Handler {
  protected def process: Process = {
    case message@Data(_, _, _) =>
      state.receiveMessage(message).map(x => HandlerResult(ReceiveMessageAction(x._2), DataHandler(x._1)))
  }

  override protected def processRequest: ProcessRequest = {
    case SendMessageRequest(message) =>
      state.sendMessage(message).map(x => HandlerResult(x._2, DataHandler(x._1)))
  }
}
