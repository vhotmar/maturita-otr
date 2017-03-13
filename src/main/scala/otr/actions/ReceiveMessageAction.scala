package otr.actions

import otr.Action

case class ReceiveMessageAction(message: Array[Byte]) extends Action
