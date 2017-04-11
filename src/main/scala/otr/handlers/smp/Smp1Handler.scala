package otr.handlers.smp

import otr.actions.ProcessAction
import otr.requests.{AnswerSmpRequest, BigIntTlv, SendMessageRequest}
import otr.utils.SMP.State.{Step1AState, Step1BState}
import otr.{Handler, HandlerResult}

import scalaz.Scalaz._

case class Smp1Handler(state: Step1AState) extends Handler {

  import otr.utils.SMP._
  import otr.utils.TupleConversions._

  override protected def process: Process = {
    case AnswerSmpRequest(secret: Array[Byte]) =>
      val r = randomExponent()
      val newState = Step1BState(state, secret, r)

      val toSend = (newState.g1.modPow(newState.x2, ModulusS) +: ZK.generateLogProof(newState.g1, newState.x2, 3).plist) :::
        (newState.g1.modPow(newState.x3, ModulusS) +: ZK.generateLogProof(newState.g1, newState.x3, 4).plist) :::
        List(newState.p, newState.q) :::
        ZK.generateLogCoordsProof(newState.g1, newState.g2, newState.g3, newState.secret, r, 5).plist

      HandlerResult(ProcessAction(SendMessageRequest(Array.empty, List(BigIntTlv(3, toSend)))), Smp3Handler(newState)).right
  }

}
