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

  val currentChatId = chatsState.currentChatId

  val currentChat: ObjectBinding[Option[ChatState]] = Bindings.createObjectBinding(() => {
    chatsState.chats.get(currentChatId.value)
  }, currentChatId, chatsState.chats)

  val hasChat: BooleanBinding = Bindings.createBooleanBinding(() => currentChat.value.isDefined, currentChat)

  val currentMessage = new StringProperty("")

  def addContact(n: String): Unit = {
    chatService
      .connectTo(n)
      .onFailure({
        case e =>
          messageService.error(s"Could not connect to $n")
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

  def prove() = {
    for {
      question <- messageService.text("Question for your secret")
      secret <- messageService.text("Your secret")

      r = chatService.requestSmp(chatsState.currentChatId.value, secret, question)
    } yield r
  }
}
