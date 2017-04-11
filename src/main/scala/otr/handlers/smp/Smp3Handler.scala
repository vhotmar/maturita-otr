package otr.handlers.smp

import otr.actions.{ProcessAction, ResultSmpAction}
import otr.requests.{BigIntTlv, SendMessageRequest, UBigIntTlv}
import otr.utils.SMP.State.Step1BState
import otr.{Handler, HandlerResult, REmpty}

case class Smp3Handler(state: Step1BState) extends Handler {

  import otr.utils.SMP._
  import otr.utils.TupleConversions._
  import otr.utils.Validate._

  override protected def process: Process = {
    case UBigIntTlv(4, pa :: qa :: c1 :: d1 :: d2 :: ra :: c2 :: d3 :: _) =>
      val r = randomExponent()

      val qab = (qa * state.q.modInverse(ModulusS)).mod(ModulusS)
      val rb = qab.modPow(state.x3, ModulusS)
      val rab = ra.modPow(state.x3, ModulusS)
      val pab = (pa * state.p.modInverse(ModulusS)).mod(ModulusS)
      val eq = pab.compare(rab) == 0


      for {
        _ <- (List(pa, qa, ra).forall(checkGroupElem) && List(d1, d2, d3).forall(checkExpon)).validate("Invalid parameter")
        _ <- ZK.checkLogCoordsProof(c1, d1, d2, state.g1, state.g2, state.g3, qa, pa, 6).validate("Invalid parameter")
        _ <- ZK.checkLogEqualProof(c2, d3, state.g1, state.g3o, qab, ra, 7).validate("Proof checking failed")

        toSend = List(rb) ::: ZK.generateLogEqualProof(state.g1, state.x3, qab, 8).plist
      } yield HandlerResult(
        List(
          ProcessAction(SendMessageRequest(Array.empty, List(BigIntTlv(5, toSend)))),
          ResultSmpAction(eq)
        ),
        REmpty(),
        NoSmpHandler(state.keys)
      )
  }
}
