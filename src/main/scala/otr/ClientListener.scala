package otr

trait ClientListener extends Receiver {
  def smpResult(res: Boolean)

  def smpAbort()

  def smpRequestReceived(question: Option[String])
}
