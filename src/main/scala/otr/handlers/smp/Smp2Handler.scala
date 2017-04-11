package otr.handlers.smp

import otr.actions.ProcessAction
import otr.requests.{BigIntTlv, SendMessageRequest, UBigIntTlv}
import otr.utils.SMP.State.{Step1State, Step2State}
import otr.{Handler, HandlerResult}

case class Smp2Handler(state: Step1State) extends Handler {

  import otr.utils.SMP._
  import otr.utils.TupleConversions._
  import otr.utils.Validate._

  override protected def process: Process = {
    case UBigIntTlv(3, g2b :: c2 :: d2 :: g3b :: c3 :: d3 :: pb :: qb :: cp :: d5 :: d6 :: _) =>
      val r = randomExponent()

      val newState = Step2State(state, g2b, g3b, pb, qb, r)

      for {
        _ <- (List(g2b, g3b, pb, qb).forall(checkGroupElem) && List(d2, d3, d5, d6).forall(checkExpon)).validate("Invalid parameters")
        _ <- (ZK.checkLogProof(c2, d2, newState.g1, g2b, 3) && ZK.checkLogProof(c3, d3, newState.g1, g3b, 4)).validate("Proof checking failed")
        _ <- ZK.checkLogCoordsProof(cp, d5, d6, newState.g1, newState.g2, newState.g3, qb, pb, 5).validate("Invalid parameter")

        toSend = List(newState.p, newState.q) :::
          ZK.generateLogCoordsProof(newState.g1, newState.g2, newState.g3, newState.secret, r, 6).plist :::
          List(newState.ra) :::
          ZK.generateLogEqualProof(newState.g1, newState.x3, newState.qab, 7).plist
      } yield HandlerResult(ProcessAction(SendMessageRequest(Array.empty, List(BigIntTlv(4, toSend)))), Smp4Handler(newState))
  }
}
