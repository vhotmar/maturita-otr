package utils

import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector}
import scodec.{Attempt, Codec, DecodeResult}
import utils.Results.{FResult, None}

trait Parsable[Config] {
  self =>

  import otr.utils.AttemptConversions._

  type E >: self.type <: Parsable[Config]
  type PC <: ParsableCompanion[Config, E]

  def companion: PC

  def instance: E = self

  def encode(config: Config): FResult[BitVector] =
    companion.codec(config).encode(this)
}

trait ParsableCompanion[Config, E <: Parsable[Config]] {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  def codec(config: Config): Codec[E]

  def decode(config: Config, bytes: Array[Byte]): FResult[E] =
    codec(config).decode(bytes)
}

trait EParsable extends Parsable[None] {
  self =>

  import otr.utils.AttemptConversions._

  type E >: self.type <: EParsable
  type PC = EParsableCompanion[E]

  def encode: FResult[BitVector] =
    companion.codec.encode(this)
}

trait EParsableCompanion[E <: EParsable] extends ParsableCompanion[None, E] {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  def decode(bytes: Array[Byte]): FResult[E] =
    codec.decode(bytes)

  def codec: Codec[E]

  def codec(config: None): Codec[E] = codec
}

trait CommandParsable[Config, Hash, P <: Parsable[Config]] {

  type CommandCompanion = CommandParsableCompanion[Config, Hash, _ <: P]
  val commandCompanion: CommandCompanion

  def codec(config: Config): Codec[P] = {
    def encode(msg: P): Attempt[BitVector] = {
      val c = msg.companion.codec(config)

      for {
        encodedHeader <- encodeHeader(msg, config)
        data <- c.encode(msg)
      } yield encodedHeader ++ data
    }

    def decode(bits: BitVector): Attempt[DecodeResult[P]] =
      for {
        meta <- decodeHeader(bits, config)
        (command, rest) = meta
        msg <- decodePayload(rest, config, command)
      } yield msg

    Codec[P](encode _, decode _)
  }

  def decodePayload(payload: BitVector, config: Config, command: Hash): Attempt[DecodeResult[P]] = {
    val cmd = commandCompanion.byCommand(command)

    cmd.codec(config).decode(payload).flatMap(p =>
      if (!p.remainder.isEmpty) Failure(scodec.Err("command message length did not match"))
      else Successful(p))
  }

  def encodeHeader(msg: P, config: Config): Attempt[BitVector]

  def decodeHeader(bits: BitVector, config: Config): Attempt[(Hash, BitVector)]
}

trait BCommandParsable[Config, P <: Parsable[Config]] extends CommandParsable[Config, ByteVector, P] {}

trait CommandParsableCompanion[Config, Hash, P <: Parsable[Config]] {
  type PC <: ParsableCompanion[Config, _ <: P]
  type All = Set[PC]

  val all: Set[PC]

  val byCommand: Map[Hash, PC] = {
    require(all.map(command).size == all.size, "Type headers must be unique")

    all.map { companion => command(companion) -> companion }.toMap
  }

  def command(o: PC): Hash
}

trait BCommandParsableCompanion[Config, P <: Parsable[Config]] extends CommandParsableCompanion[Config, ByteVector, P]
