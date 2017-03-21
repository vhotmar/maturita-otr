package gui.controllers

import java.net.InetSocketAddress

import network.ClientListener

import scalafx.beans.property.BooleanProperty

class GUIState {
  val connecting = BooleanProperty(false)
  val registering = BooleanProperty(false)

  val connected = BooleanProperty(false)
  val registered = BooleanProperty(false)

  val ready = BooleanProperty(false)
  ready <== connected && registered // ready only when connected and registered
}

class StateObserver(state: GUIState) extends ClientListener {
  override def connected(): Unit = {
    state.connecting.value = false
    state.connected.value = true
  }

  override def connecting(addr: InetSocketAddress): Unit = {
    state.connecting.value = true
    state.connected.value = false
  }

  override def connectionFailed(): Unit = {
    state.connecting.value = false
    state.connected.value = false
  }

  override def reg

  override def registered(name: String, id: Int): Unit = {
    state.registering.value = false
    state.registered.value = true
  }

  override def nameTaken(name: String): Unit = {
    state.registering.value = false
    state.registered.value = false
  }

}
