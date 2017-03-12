package otr

trait Receiver {
  def receive(bytes: Array[Byte]): FResult[Any]
}
