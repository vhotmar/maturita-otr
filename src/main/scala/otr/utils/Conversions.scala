package otr.utils

import java.security.PublicKey

import otr.ParseError
import otr.messages.types.Data
import scodec.bits.{BitVector, ByteVector}
import scodec.{Attempt, DecodeResult}

import scalaz._

object ByteVectorConversions {
  implicit def byteVectorToByteArray(vec: ByteVector): Array[Byte] = vec.toArray

  implicit def byteArrayToByteVector(arr: Array[Byte]): ByteVector = ByteVector.view(arr)
}

object PublicKeyConversions {

  import ByteVectorConversions._

  implicit def publicKeyToData(publicKey: PublicKey): Data[PublicKey] = Data(publicKey.getEncoded, publicKey)
}

object BitVectorConversions {
  implicit def bitVectorToByteArray(vec: BitVector): Array[Byte] = vec.toByteArray

  implicit def byteArrayToBitVector(arr: Array[Byte]): BitVector = BitVector.view(arr)
}

object AttemptConversions {
  implicit def attemptToEither[A](attempt: Attempt[A]): Throwable \/ A =
    attempt.fold(err => -\/(ParseError(err)), a => \/-(a))

  implicit def attemptToEitherDecodedResult[A](attempt: Attempt[DecodeResult[A]]): Throwable \/ A =
    attempt.fold(err => -\/(ParseError(err)), a => \/-(a.value))
}
