package gui.model

import org.joda.time.DateTime
import otr.Client

import scalafx.collections.ObservableBuffer

case class ChatState(id: Int, name: String, client: Client) {

  import utils.Joda._

  val started: DateTime = DateTime.now()
  val messages: ObservableBuffer[ChatMessage] = ObservableBuffer.empty[ChatMessage]

  def latestMessage: ObservableBuffer[ChatMessage] = messages.sortBy(_.date)
}

case class ChatMessage(fromId: Int, toId: Int, message: String) {
  val date: DateTime = DateTime.now()
}
