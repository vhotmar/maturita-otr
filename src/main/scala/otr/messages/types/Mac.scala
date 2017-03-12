package otr.messages.types

import otr.FResult
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.bits.ByteVector

case class Mac(bytes: ByteVector) {
  def verify(key: ByteVector, v: ByteVector): FResult[Boolean] =
    Crypto.verifyMac(key, bytes, v)
}

object Mac {
  def create(bytes: ByteVector, key: ByteVector, length: Long = 32): FResult[Mac] =
    Crypto.hmac(bytes, key).map(mac => new Mac((mac: ByteVector).take(20)))
}