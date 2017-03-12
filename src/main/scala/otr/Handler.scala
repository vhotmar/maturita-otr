package otr

import scalaz.Scalaz._

case class HandlerResult(messages: List[Message], newHandler: Handler)

object HandlerResult {
  def apply(message: Message, newHandler: Handler): HandlerResult =
    new HandlerResult(List(message), newHandler)
}

trait Handler {
  type Result = FResult[otr.HandlerResult]
  type Process = scala.PartialFunction[Message, Result]


  protected def process: Process

  def handle(data: Any): Result = data match {
    case message: Message =>
      if (process isDefinedAt message)
        process(message)
      else
        HandlerResult(List(), this).right

    case _ => HandlerResult(List(), this).right
  }

  /*
  def handle(data: Any): Either[Any, Result] = data match {
    case e: Message => this.process(e)
    case _ => Left("Don't know this structure")
  }
  */
}
