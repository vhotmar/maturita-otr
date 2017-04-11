package gui.view

import gui.internal.{DisableSelectionModel, ViewLoader}
import gui.model.{ChatMessage, ChatState}
import gui.view.model.ChatsViewModel

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafxml.core.macros.sfxml

@sfxml
class ChatsView(
                 contacts: ListView[ChatState],
                 messages: ListView[ChatMessage],
                 viewLoader: ViewLoader,
                 message: TextArea,
                 model: ChatsViewModel,
                 sendMessageButton: Button,
                 proveButton: Button
               ) {
  contacts.items = model.chats
  contacts.focusTraversable = false
  contacts.selectionModel = new DisableSelectionModel[ChatState]

  contacts.cellFactory = (_) => {
    new ListCell[ChatState]() {
      protected val view = new ContactView(model.currentChatId)

      editable.value = false
      prefWidth <== contacts.width - 2
      maxWidth <== prefWidth

      graphic.delegate.set(view)

      view.onMouseClicked = (e: MouseEvent) => {
        if (item != null && item.value != null)
          model.onContactChange(item.value)
      }

      item.onChange((_, _, newValue) => {
        view.model.value = newValue
      })
    }
  }

  messages.focusTraversable = false
  messages.selectionModel = new DisableSelectionModel[ChatMessage]

  messages.cellFactory = (_) => {
    new ListCell[ChatMessage]() {
      protected val view = new MessageView(model.clientState)

      editable.value = false
      prefWidth <== messages.width - 2
      maxWidth <== prefWidth

      graphic.delegate.set(view)

      item.onChange((_, _, newMessage) => {
        view.model.value = newMessage
      })
    }
  }

  model.currentChat.onChange((_, _, newChat) => newChat match {
    case Some(chat) =>
      messages.items = chat.messages
    case None =>
      messages.items = ObservableBuffer.empty
  })

  message.text <==> model.currentMessage
  message.onKeyPressed = (key: KeyEvent) => {
    if (key.code.equals(KeyCode.Enter)) {
      key.consume()

      if (key.shiftDown) {
        message.text.value = message.text.value + "\n"
      } else
        model.sendMessage()
    }
  }

  message.disable.bind(model.hasChat.not())
  sendMessageButton.disable.bind(model.hasChat.not())
  proveButton.disable.bind(model.hasChat.not())

  def addContact(): Unit = {
    // TODO: Better would be simple popup
    val dialog = new TextInputDialog() {
      title = "Add contact"
      headerText = "Add your contact name."
      contentText = "Please enter your contact name:"
    }

    val result = dialog.showAndWait()

    result match {
      case Some(n) => model.addContact(n)
      case None =>
    }
  }

  def sendMessage(): Unit = {
    model.sendMessage()
  }

  def prove(): Unit = {
    model.prove()
  }
}
