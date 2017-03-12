package utils

import scala.collection.mutable

trait FSM[S, D] {

  import FSM._

  type State = FSM.State[S, D]
  type Event = FSM.Event[D]
  type StateFunction = scala.PartialFunction[Event, State]

  private var currentState: State = _
  private val stateFunctions = mutable.Map[S, StateFunction]()

  //noinspection ForwardReference
  private var handleEvent: StateFunction = handleEventDefault

  def when(stateName: S)(stateFunction: StateFunction): Unit =
    register(stateName, stateFunction)

  def whenUnhandled(stateFunction: StateFunction): Unit =
    handleEvent = handleEvent orElse stateFunction

  private def register(name: S, function: StateFunction): Unit =
    if (stateFunctions contains name) stateFunctions(name) = stateFunctions(name) orElse function
    else stateFunctions(name) = function

  def startWith(stateName: S, stateData: D): Unit =
    currentState = FSM.State(stateName, stateData)

  def goto(nextStateName: S): State = FSM.State(nextStateName, currentState.stateData)

  def stay(): State = goto(currentState.stateName)

  def process(value: Any): Unit = {
    val event = Event(value, currentState.stateData)

    processEvent(event)
  }

  private def processEvent(event: Event) = {
    val stateFunction = stateFunctions(currentState.stateName)

    val nextState =
      if (stateFunction isDefinedAt event) stateFunction(event)
      else handleEvent(event)

    currentState = nextState
  }

  private val handleEventDefault: StateFunction = {
    case Event(_, _) ⇒
      stay()
  }

}

object FSM {

  case class State[S, D](stateName: S, stateData: D)

  case class Event[D](event: Any, stateData: D)

}