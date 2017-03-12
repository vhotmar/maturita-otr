package otr.messages.types

import otr.FResult
import otr.utils.ByteVectorConversions._
import otr.utils.Crypto
import scodec.bits.ByteVector

case class Encrypted(bytes: ByteVector) {
  def decrypt(key: ByteVector, ctr: ByteVector = Crypto.ZeroCtr): FResult[Array[Byte]] =
    Crypto.decryptAES(bytes, key, ctr)
}

object Encrypted {
  def create(bytes: ByteVector, key: ByteVector, ctr: ByteVector = Crypto.ZeroCtr): FResult[Encrypted] =
    Crypto.encryptAES(bytes, key, ctr).map(new Encrypted(_))
}