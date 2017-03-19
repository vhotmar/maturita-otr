package otr.utils

import otr.messages._
import scodec.Attempt
import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.{bytes, uint16, uint32}
import utils.{BCommandParsable, BCommandParsableCompanion}

trait Message extends utils.Parsable[MessageConfig] {
  self =>
  type E >: self.type <: Message
  type PC = MessageCompanion[E]
}

trait MessageCompanion[E <: Message] extends utils.ParsableCompanion[MessageConfig, E] {
  def command: ByteVector
}

case class MessageConfig(version: Int, senderTag: Int, receiverTag: Int)

object Message extends BCommandParsable[MessageConfig, Message] {
  val commandCompanion = MessageCompanion

  def encodeHeader(msg: Message, config: MessageConfig): Attempt[BitVector] = {
    for {
      version <- uint16.encode(config.version)
      command <- bytes(1).encode(msg.companion.command)
      senderTag <- uint32.encode(config.senderTag)
      receiverTag <- uint32.encode(config.receiverTag)
    } yield version ++ command ++ senderTag ++ receiverTag
  }

  def decodeHeader(bits: BitVector, config: MessageConfig): Attempt[(ByteVector, BitVector)] = {
    for {
      version <- uint16.decode(bits).flatMap { version =>
        if (version.value == config.version) Successful(version)
        else Failure(scodec.Err("versions did not match"))
      }
      c <- bytes(1).decode(version.remainder)
      command = c.value
      st <- uint32.decode(c.remainder)
      senderTag = st.value
      rt <- uint32.decode(st.remainder)
      receiverTag = rt.value
    } yield (command, rt.remainder)
  }
}

object MessageCompanion extends BCommandParsableCompanion[MessageConfig, Message] {
  type PC = MessageCompanion[_ <: Message]
  val all: All = Set(Data, DHCommit, DHKey, RevealSignature, Signature)

  def command(o: MessageCompanion[_ <: Message]): ByteVector = o.command
}
