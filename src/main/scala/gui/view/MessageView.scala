package gui.view

import gui.model.{ChatMessage, ClientState}
import org.joda.time.format.DateTimeFormat

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, Pane, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

class MessageView(clientState: ClientState) extends HBox() {
  styleClass += "messageContainer"
  visible = false

  val icon = new Circle() {
    styleClass += "icon"

    centerX = 10
    centerY = 10
    radius = 10

    fill = Color.gray(0.9)
    stroke = Color.gray(0.4)
    strokeWidth = 1

    margin = Insets(4, 10, 4, 4)
  }

  val message = new Label() {
    styleClass += "text"

    wrapText = true
  }

  val messageC = new Pane() {
    styleClass += "message"

    hgrow = Priority.Always

    children = Seq(message)
  }

  val date = new Label() {
    styleClass += "date"
  }

  val data = new VBox() {
    children = Seq(messageC, date)

    hgrow = Priority.Always
  }

  children = Seq(
    icon
  )

  val model: ObjectProperty[ChatMessage] = new ObjectProperty[ChatMessage]()

  model.onChange((_, oldValue, newValue) => {
    if (newValue == null) {
      visible = false
    } else if (oldValue != newValue) {
      visible = true

      message.text = newValue.message
      date.text = newValue.date.toString(DateTimeFormat.mediumTime())

      styleClass -= "sending"
      styleClass -= "receiving"

      if (clientState.id.value == newValue.fromId) {
        children = Seq(icon, data)
        styleClass += "sending"
      } else {
        children = Seq(data, icon)
        styleClass += "receiving"
      }
    }
  })
}
