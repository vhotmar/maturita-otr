package gui.view.model

import gui.internal.ObservableMapValues
import gui.model.{ChatState, ChatsState, ClientState}
import gui.services.{ChatService, MessageService}

import scala.concurrent.ExecutionContext.Implicits.global
import scalafx.beans.binding.{Bindings, BooleanBinding, ObjectBinding}
import scalafx.beans.property.StringProperty

class ChatsViewModel(
                      chatsState: ChatsState,
                      val clientState: ClientState,
                      chatService: ChatService,
                      messageService: MessageService
                    ) {
  val chats = ObservableMapValues(chatsState.chats)

  val currentChat: ObjectBinding[Option[ChatState]] = Bindings.createObjectBinding(() => {
    chatsState.chats.get(chatsState.currentChatId.value)
  }, chatsState.currentChatId, chatsState.chats)

  val hasChat: BooleanBinding = Bindings.createBooleanBinding(() => currentChat.value.isDefined, currentChat)

  val currentMessage = new StringProperty("")

  def addContact(n: String): Unit = {
    chatService
      .connectTo(n)
      .onFailure({
        case e =>
          messageService.error(s"Could not connect to $n")

          println(e)
      })
  }

  def sendMessage(): Unit = {
    val m = currentMessage.value

    if (hasChat.value && !m.isEmpty) {
      chatService.sendMessage(chatsState.currentChatId.value, m)

      currentMessage.value = ""
    }
  }

  def onContactChange(chatState: ChatState): Unit = {
    chatsState.currentChatId.value = chatState.id
  }
}
