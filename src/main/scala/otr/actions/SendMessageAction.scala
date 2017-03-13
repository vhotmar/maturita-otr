package otr.actions

import otr.Action
import otr.utils.Message

case class SendMessageAction(message: Message) extends Action
