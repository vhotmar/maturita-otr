package otr.messages.types

import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.bits.ByteVector
import utils.Results.FResult

case class Mac(bytes: ByteVector) {
  def verify(bytesToVerify: ByteVector, key: ByteVector): FResult[Boolean] =
    Crypto.verifyMac(bytesToVerify, key, bytes)
}

object Mac {
  def create(bytes: ByteVector, key: ByteVector, length: Long = 32): FResult[Mac] =
    Crypto.hmac(bytes, key).map(mac => new Mac((mac: ByteVector).take(20)))
}