package otr

import _root_.utils.Results.FResult

trait Receiver {
  def receive(bytes: Array[Byte]): FResult[Any]
}
