package gui.model

import scalafx.beans.property.{BooleanProperty, IntegerProperty, StringProperty}

trait ClientState {
  val connecting = BooleanProperty(false)
  val connected = BooleanProperty(false)

  val registering = BooleanProperty(false)
  val registered = BooleanProperty(false)

  val name = StringProperty("")
  val id = IntegerProperty(-1)

  val ready = BooleanProperty(false)

  ready <== connected and registered
}
