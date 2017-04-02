package gui.model

import scalafx.beans.property.IntegerProperty
import scalafx.collections.ObservableMap

trait ChatsState {
  val chats: ObservableMap[Int, ChatState] = ObservableMap.empty[Int, ChatState]
  val currentChatId: IntegerProperty = IntegerProperty(-1)
}
