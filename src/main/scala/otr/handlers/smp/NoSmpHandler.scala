package otr.handlers.smp

import otr.actions._
import otr.requests.BigIntTlv._
import otr.requests._
import otr.utils.SMP.State._
import otr.{Handler, HandlerResult, REmpty, ValidationError}
import scodec.bits.{BitVector, ByteVector}
import utils.Results.FResult

import scalaz.Scalaz._

case class NoSmpHandler(keys: Keys) extends Handler {

  import otr.utils.AttemptConversions._
  import otr.utils.SMP._
  import otr.utils.TupleConversions._
  import otr.utils.Validate._

  override protected def process: Process = {
    case InitSmpRequest(secret: Array[Byte], question: Option[Array[Byte]]) =>
      val state = Step1State(keys, secret)

      val g2a = state.g1.modPow(state.x2, ModulusS)
      val g3a = state.g1.modPow(state.x3, ModulusS)

      val toSend = (g2a +: ZK.generateLogProof(state.g1, state.x2, 1).plist) :::
        (g3a +: ZK.generateLogProof(state.g1, state.x3, 2).plist)

      val tlv: FResult[Tlv] = {
        if (question.isDefined) {
          val encoded: FResult[BitVector] = BigIntTlv.codec.encode(toSend)

          encoded.map(x => Tlv(7, ByteVector(question.get ++ Array(0x00.toByte) ++ x.toByteArray)))
        } else {
          val tlv: Tlv = BigIntTlv(2, toSend)

          tlv.right
        }
      }

      for {
        t <- tlv
      } yield HandlerResult(ProcessAction(SendMessageRequest(Array.empty, List(t))), Smp2Handler(state))

    case UBigIntTlv(2, g2a :: c2 :: d2 :: g3a :: c3 :: d3 :: _) =>
      for {
        _ <- (checkGroupElem(g2a) && checkGroupElem(g3a) && checkExpon(d2) && checkExpon(d3)).validate("Invalid parameters")
        _ <- (ZK.checkLogProof(c2, d2, GeneratorS, g2a, 1) && ZK.checkLogProof(c3, d3, GeneratorS, g3a, 2)).validate("Proof checking failed")
      } yield HandlerResult(ReceiveSmpAction(None), REmpty(), Smp1Handler(Step1AState(keys, GeneratorS, g2a, g3a)))

    case Tlv(7, value) =>
      val (question, dInts) = value.toArray.span(_ != 0x00.toByte)

      val ints: FResult[List[BigInt]] = BigIntTlv.codec.decode(BitVector(dInts.tail))

      ints.flatMap {
        case g2a :: c2 :: d2 :: g3a :: c3 :: d3 :: _ =>
          for {
            _ <- (checkGroupElem(g2a) && checkGroupElem(g3a) && checkExpon(d2) && checkExpon(d3)).validate("Invalid parameters")
            _ <- (ZK.checkLogProof(c2, d2, GeneratorS, g2a, 1) && ZK.checkLogProof(c3, d3, GeneratorS, g3a, 2)).validate("Proof checking failed")
          } yield HandlerResult(ReceiveSmpAction(Some(new String(question))), REmpty(), Smp1Handler(Step1AState(keys, GeneratorS, g2a, g3a)))
        case _ =>
          ValidationError("Invalid arguments").left
      }
  }
}


