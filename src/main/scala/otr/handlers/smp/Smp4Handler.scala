package otr.handlers.smp

import otr.actions.ResultSmpAction
import otr.requests.UBigIntTlv
import otr.utils.SMP.State.Step2State
import otr.{Handler, HandlerResult}

case class Smp4Handler(state: Step2State) extends Handler {

  import otr.utils.SMP._
  import otr.utils.Validate._

  override protected def process: Process = {
    case UBigIntTlv(5, rb :: c :: d :: _) =>
      val eq = rb.modPow(state.x3, ModulusS).compare(state.pab) == 0

      for {
        _ <- (checkGroupElem(rb) && checkExpon(d)).validate("Invalid parameter")
        _ <- ZK.checkLogEqualProof(c, d, state.g1, state.g3o, state.qab, rb, 8).validate("Proof checking failed")
      } yield HandlerResult(ResultSmpAction(eq), NoSmpHandler(state.keys))
  }
}
