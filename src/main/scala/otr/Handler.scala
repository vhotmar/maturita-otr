package otr

import _root_.utils.Results.FResult
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
  type Process = scala.PartialFunction[Any, Result]

  protected def process: Process

  def handle(data: Any): Result = {
    if (process isDefinedAt data)
      process(data)
    else
      HandlerResult(List.empty, this).right
  }

  def canHandle(request: Any): Boolean = process.isDefinedAt(request)
}

trait HandlerManager {
  var handler: Handler
  private var dataQueue: List[Any] = List.empty

  protected def processByHandler(data: Any): FResult[Boolean] = {
    handler
      .handle(data)
      .flatMap(handleHandlerResult)
  }

  protected def tryProcessQueued(): FResult[Boolean] = {
    val (canHandle, cantHandle) = dataQueue.partition(handler.canHandle)

    dataQueue = cantHandle

    canHandle.map(processByHandler).sequenceU.map(_ => true)
  }

  protected def queue(data: Any) = {
    dataQueue = dataQueue :+ data
  }

  protected def setHandler(newHandler: Handler): FResult[Boolean] = {
    handler = newHandler

    tryProcessQueued()
  }

  protected def handleAction(action: Action): FResult[Boolean]

  protected def handleHandlerResult(result: HandlerResult): FResult[Boolean] = {
    for {
    // Set handler
      _ <- setHandler(result.newHandler)

      // Process actions
      _ <- result.actions.map(handleAction).sequenceU
    } yield true
  }
}
