package otr.messages.types

import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.bits.ByteVector

case class Hash(bytes: ByteVector) {
  def verify(v: ByteVector): Boolean =
    Crypto.verifyHash(bytes, v)
}

object Hash {
  def apply(bytes: ByteVector, calculateHash: Boolean = false): Hash =
    if (calculateHash) new Hash(Crypto.hash(bytes))
    else new Hash(bytes)
}