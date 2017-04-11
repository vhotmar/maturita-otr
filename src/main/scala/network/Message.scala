package network

import network.messages._
import scodec.Attempt
import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._
import utils.{BCommandParsable, BCommandParsableCompanion, Parsable, ParsableCompanion}

trait Message extends Parsable[MessageConfig] {
  self =>
  type E >: self.type <: Message
  type PC = MessageCompanion[E]
}

case class MessageConfig(version: Int)

trait MessageCompanion[E <: Message] extends ParsableCompanion[MessageConfig, E] {
  def command: ByteVector
}

object Message extends BCommandParsable[MessageConfig, Message] {
  val commandCompanion = MessageCompanion

  def encodeHeader(msg: Message, config: MessageConfig): Attempt[BitVector] = {
    for {
      version <- uint16.encode(config.version)
      command <- bytes(1).encode(msg.companion.command)
    } yield version ++ command
  }

  def decodeHeader(bits: BitVector, config: MessageConfig): Attempt[(ByteVector, BitVector)] = {
    for {
      version <- uint16.decode(bits).flatMap { version =>
        if (version.value == config.version) Successful(version)
        else Failure(scodec.Err("versions did not match"))
      }
      c <- bytes(1).decode(version.remainder)
    } yield (c.value, c.remainder)
  }
}

object MessageCompanion extends {
  val all: Set[MessageCompanion[_ <: Message]] = Set(Connected, ConnectedFrom, ConnectTo, Data, Disconnect, Disconnected, Register, Registered, UserDoesNotExist, UserExists)
} with BCommandParsableCompanion[MessageConfig, Message] {
  type PC = MessageCompanion[_ <: Message]

  def command(o: MessageCompanion[_ <: Message]): ByteVector = o.command
}
