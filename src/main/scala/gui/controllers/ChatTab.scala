package gui.controllers

import gui.Message

import scalafx.scene.control.{Button, ListView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class ChatTab(
  private val messages: ListView[Message],
  private val messageText: TextField,
  private val sendMessageButton: Button
) {
}
