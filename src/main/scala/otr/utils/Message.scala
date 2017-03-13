package otr.utils

import otr.FResult
import otr.messages._
import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.{bytes, uint16, uint32}
import scodec.{Attempt, Codec, DecodeResult}

trait Message {
  self =>

  import otr.utils.AttemptConversions._

  type E >: self.type <: Message

  def companion: MessageCompanion[E]

  def instance: E = self

  def encode(version: Int): FResult[BitVector] =
    companion.codec(version).encode(this)

  def encode(version: Int, senderTag: Int, receiverTag: Int): FResult[BitVector] =
    Message.codec(version, senderTag, receiverTag).encode(this)
}

trait MessageCompanion[E <: Message] {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  def codec(version: Int): Codec[E]

  def command: ByteVector

  def decode(version: Int, bytes: Array[Byte]): FResult[E] =
    codec(version).decode(bytes)
}

object Message {
  def codec(version: Int, senderTag: Int, receiverTag: Int): Codec[Message] = {
    def encode(msg: Message) = {
      val c = msg.companion.codec(version)

      for {
        version <- uint16.encode(version)
        command <- bytes(1).encode(msg.companion.command)
        senderTag <- uint32.encode(senderTag)
        receiverTag <- uint32.encode(receiverTag)
        data <- c.encode(msg)
      } yield version ++ command ++ senderTag ++ receiverTag ++ data
    }

    def decode(bits: BitVector) =
      for {
        metadata <- decodeHeader(bits, version, senderTag, receiverTag)
        (command, _, _, rest) = metadata
        msg <- decodePayload(rest, version, command)
      } yield msg

    Codec[Message](encode _, decode _)
  }

  def decodeHeader(bits: BitVector, clientVersion: Int, senderTag: Int, receiverTag: Int): Attempt[(ByteVector, Long, Long, BitVector)] =
    for {
      version <- uint16.decode(bits).flatMap { version =>
        if (version.value == clientVersion) Successful(version)
        else Failure(scodec.Err("versions did not match"))
      }
      c <- bytes(1).decode(version.remainder)
      command = c.value
      st <- uint32.decode(c.remainder)
      senderTag = st.value
      rt <- uint32.decode(st.remainder)
      receiverTag = rt.value
    } yield (command, senderTag, receiverTag, rt.remainder)

  def decodePayload(payload: BitVector, clientVersion: Int, command: ByteVector): Attempt[DecodeResult[Message]] = {
    val cmd = MessageCompanion.byCommand(command)
    cmd.codec(clientVersion).decode(payload).flatMap(p =>
      if (!p.remainder.isEmpty) Failure(scodec.Err("command message length did not match"))
      else Successful(p))
  }
}

object MessageCompanion {
  // TODO: investigate automatic class discovery
  val all: Set[MessageCompanion[_ <: Message]] = Set(Data, DHCommit, DHKey, Empty, RevealSignature, Signature)

  val byCommand: Map[ByteVector, MessageCompanion[_ <: Message]] = {
    require(all.map(_.command).size == all.size, "Type headers must be unique.")
    all.map { companion => companion.command -> companion }.toMap
  }
}
