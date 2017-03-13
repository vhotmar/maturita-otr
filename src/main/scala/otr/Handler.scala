package otr

import otr.actions.SendMessageAction
import otr.utils.Message

import scalaz.Scalaz._

case class HandlerResult(actions: List[Action], newHandler: Handler)

object HandlerResult {
  def apply(message: Message, newHandler: Handler): HandlerResult =
    new HandlerResult(List(SendMessageAction(message)), newHandler)

  def apply(action: Action, newHandler: Handler): HandlerResult =
    new HandlerResult(List(action), newHandler)
}

trait Handler {
  type Result = FResult[otr.HandlerResult]
  type Process = scala.PartialFunction[Message, Result]
  type ProcessRequest = scala.PartialFunction[Request, Result]

  protected def process: Process

  protected def processRequest: ProcessRequest = PartialFunction.empty

  def handle(data: Any): Result = data match {
    case message: Message =>
      if (process isDefinedAt message)
        process(message)
      else
        HandlerResult(List.empty, this).right

    case _ => HandlerResult(List.empty, this).right
  }

  def canHandleRequest(request: Request): Boolean = processRequest.isDefinedAt(request)

  def handleRequest(request: Request): Result =
    if (processRequest isDefinedAt request)
      processRequest(request)
    else
      HandlerResult(List.empty, this).right
}
