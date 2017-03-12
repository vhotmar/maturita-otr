package otr.messages

import otr.{Message, MessageCompanion}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

case class Empty(a: Int = 1) extends Message {
  type E = Empty

  def companion = Empty
}

object Empty extends MessageCompanion[Empty] {

  def codec(version: Int): Codec[Empty] = {
    "a" | int32
  }.as[Empty]

  def command: ByteVector = hex"0xfe"
}


