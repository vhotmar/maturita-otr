package otr

import java.security.PublicKey

import otr.messages.types.{Encrypted, Hash, Mac}
import otr.utils.Crypto
import scodec.Attempt.{Failure, Successful}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

object Types {

  import otr.utils.ByteVectorConversions._

  val data: Codec[ByteVector] = variableSizeBytesLong(uint32, bytes).as[ByteVector]

  val encrypted: Codec[Encrypted] = data.xmap[Encrypted](
    (bytes) => Encrypted(bytes),
    (data) => data.bytes
  ).as[Encrypted]

  val publicDSAKey: Codec[PublicKey] = data.exmap[PublicKey](
    (bytes) => {
      Crypto.parseDSAKey(bytes).fold(
        e => Failure(scodec.Err(e.getMessage)),
        key => Successful(key)
      )
    },
    (data) => Successful(data.getEncoded)
  ).as[PublicKey]

  val publicECKey: Codec[PublicKey] = data.exmap[PublicKey](
    (bytes) => {
      Crypto.parseECKey(bytes).fold(
        e => Failure(scodec.Err(e.getMessage)),
        key => Successful(key)
      )
    },
    (data) => Successful(data.getEncoded)
  ).as[PublicKey]

  val hash: Codec[Hash] = data.xmap[Hash](
    (data) => Hash(data),
    (hash) => hash.bytes
  ).as[Hash]

  val mac: Codec[Mac] = data.xmap[Mac](
    (data) => Mac(data),
    (mac) => mac.bytes
  ).as[Mac]

  def mac(length: Int): Codec[Mac] = bytes(length).xmap[Mac](
    (data) => Mac(data),
    (mac) => mac.bytes
  ).as[Mac]
}

