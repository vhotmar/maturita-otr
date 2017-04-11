package otr.handlers

import otr._
import otr.actions.{AbortSmpAction, ProcessAction, ReceiveMessageAction}
import otr.handlers.smp.NoSmpHandler
import otr.messages.Data
import otr.requests.{AbortSmpRequest, SendMessageRequest, Tlv}
import otr.state.DataState
import otr.utils.SMP.State.Keys
import scodec.bits.ByteVector

import scalaz.Scalaz._

case class DataHandler(state: DataState, smpHandler: Handler) extends Handler {
  override def canHandle(request: Any): Boolean = processData.isDefinedAt(request) || smpHandler.canHandle(request)

  private def processData: Process = {
    case message@Data(_, _, _) =>
      for {
      // decode message
        r <- state.receiveMessage(message)
        (state, dMessage) = r

        // get tlvs from the message
        (message, dTlvs) = dMessage.span(_ != 0x00.toByte)
        tlvs <- Tlv.decodeList(dTlvs.tail)

        actions = tlvs.map(x => ProcessAction(x))
      } yield HandlerResult(ReceiveMessageAction(message) :: actions, REmpty(), DataHandler(state, smpHandler))


    case SendMessageRequest(message, tlvs) =>
      for {
        encoded <- Tlv.encodeList(tlvs)

        r <- state.sendMessage((message :+ 0x00.toByte) ++ encoded.toByteArray)

        (state, message) = r
      } yield HandlerResult(message, RMessage(message), DataHandler(state, smpHandler))

    case Tlv(6, _) =>
      HandlerResult(AbortSmpAction(), DataHandler(state)).right

    case AbortSmpRequest() =>
      HandlerResult(
        ProcessAction(
          SendMessageRequest(Array.empty, List(Tlv(7, ByteVector(Array.empty[Byte]))))
        ),
        DataHandler(state)
      ).right
  }

  protected def process: Process = processData orElse processSmp

  private def processSmp: Process = {
    case e =>
      smpHandler.handle(e).map(result =>
        HandlerResult(result.actions, result.returned, DataHandler(state, result.newHandler)))
  }
}

object DataHandler {
  def apply(state: DataState): DataHandler =
    new DataHandler(
      state,
      NoSmpHandler(
        Keys(
          state.localLongTermKeyPair.getPublic,
          state.remoteLongTermPublicKey,
          state.parameters.ssid
        )
      )
    )
}
