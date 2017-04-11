package otr

import _root_.utils.Results.{FResult, Success}
import otr.actions.{ProcessAction, SendMessageAction}
import otr.utils.Message

import scalaz.Scalaz._

abstract class Returned()

case class REmpty() extends Returned

case class RQueued() extends Returned

case class RMessage(message: Message) extends Returned

case class HandlerResult(actions: List[Action], returned: Returned, newHandler: Handler)

object HandlerResult {
  def apply(message: Message, newHandler: Handler): HandlerResult =
    new HandlerResult(List(SendMessageAction(message)), REmpty(), newHandler)

  def apply(action: Action, newHandler: Handler): HandlerResult =
    new HandlerResult(List(action), REmpty(), newHandler)

  def apply(message: Message, returned: Returned, newHandler: Handler): HandlerResult =
    new HandlerResult(List(SendMessageAction(message)), returned, newHandler)

  def apply(action: Action, returned: Returned, newHandler: Handler): HandlerResult =
    new HandlerResult(List(action), returned, newHandler)
}

trait Handler {
  type Result = FResult[otr.HandlerResult]
  type Process = scala.PartialFunction[Any, Result]

  def handle(data: Any): Result = {
    if (process isDefinedAt data)
      process(data)
    else
      HandlerResult(List.empty, REmpty(), this).right
  }

  def canHandle(request: Any): Boolean = process.isDefinedAt(request)

  protected def process: Process
}

trait HandlerManager {
  var handler: Handler
  private var dataQueue: List[Any] = List.empty

  protected def processByHandler(data: Any): FResult[Returned] = {
    val r = handler
      .handle(data)
      .flatMap(handleHandlerResult)

    if (r.isLeft)
      println(r)

    r
  }

  protected def processByHandlerOrQueue(data: Any): FResult[Returned] = {
    if (handler.canHandle(data))
      processByHandler(data)
    else {
      queue(data)

      RQueued().right
    }
  }

  protected def tryProcessQueued(): FResult[Success] = {
    val (canHandle, cantHandle) = dataQueue.partition(handler.canHandle)

    dataQueue = cantHandle

    canHandle.map(processByHandler).sequenceU.map(_ => Success())
  }

  protected def queue(data: Any): Unit = {
    dataQueue = dataQueue :+ data
  }

  protected def setHandler(newHandler: Handler): FResult[Success] = {
    handler = newHandler

    tryProcessQueued()
  }

  protected def handleAction(action: Action): FResult[Success]

  protected def handleHandlerResult(result: HandlerResult): FResult[Returned] = {
    for {
    // Set handler
      _ <- setHandler(result.newHandler)

      // Process actions
      _ <- result.actions.map(handlePAction).sequenceU
    } yield result.returned
  }

  private def handlePAction(action: Action): FResult[Success] = {
    action match {
      case ProcessAction(data) => processByHandler(data).map(_ => Success())
      case e => handleAction(e)
    }
  }
}

