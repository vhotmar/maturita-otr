package otr

import otr.utils.Crypto
import scodec.Codec
import scodec.bits.BitVector

trait Parsable {
  self =>

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  type E >: self.type <: Parsable

  def companion: ParsableCompanion[E]

  def instance: E = self

  def encrypt(key: Array[Byte]): FResult[Array[Byte]] =
    encode.flatMap(bytes => Crypto.encryptAES(bytes, key))

  def encode: FResult[BitVector] = companion.codec.encode(this)
}

trait ParsableCompanion[E <: Parsable] {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  def codec: Codec[E]

  def decode(bytes: Array[Byte]): FResult[E] =
    codec.decode(bytes)
}
