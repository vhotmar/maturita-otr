package otr.actions

case class InitSMPAction(secret: Array[Byte], question: Option[Array[Byte]])
