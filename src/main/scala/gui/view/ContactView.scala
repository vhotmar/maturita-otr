package gui.view

import gui.model.ChatState

import scalafx.beans.binding.Bindings
import scalafx.beans.property.{IntegerProperty, ObjectProperty}
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

class ContactView(currentChatId: IntegerProperty) extends HBox() {
  styleClass += "contact"
  visible = false

  val icon = new Circle() {
    styleClass += "icon"

    centerX = 15
    centerY = 15
    radius = 15

    fill = Color.gray(0.9)
    stroke = Color.gray(0.4)
    strokeWidth = 1

    margin = Insets(7)
  }
  val name = new Label() {
    styleClass += "name"
  }

  val latestMessage = new Label() {
    styleClass += "message"
  }

  val info = new VBox() {
    children = Seq(name, latestMessage)
  }

  children = Seq(
    icon, info
  )

  val model: ObjectProperty[ChatState] = new ObjectProperty[ChatState]()

  val b = Bindings.createObjectBinding(() => {
    val styles = List("contact")

    if (currentChatId.value != null && model.value != null && currentChatId.value == model.value.id)
      styles :+ "active"
    else
      styles
  }, currentChatId, model)

  b.onChange((_, _, n) => {
    styleClass.clear()

    n.foreach(x => styleClass += x)
  })

  model.onChange((_, oldValue, newValue) => {
    if (newValue == null) {
      visible = false
    } else if (oldValue != newValue) {
      visible = true

      name.text = newValue.name

      val latestMessageBinding = Bindings.createStringBinding(
        () => if (newValue.messages.nonEmpty) newValue.messages.last.message else "",
        newValue.messages
      )

      latestMessage.text.bind(latestMessageBinding)
    }
  })
}
