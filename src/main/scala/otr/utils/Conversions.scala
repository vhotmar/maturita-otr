package otr.utils

import java.security.Key

import otr.{InvalidArgumentError, ParseError}
import scodec.bits.{BitVector, ByteVector}
import scodec.{Attempt, DecodeResult}
import utils.Results.FResult

import scalaz.Scalaz._
import scalaz._

object TupleConversions {
  implicit def productToProductOps(a: Product) = new ProductOps(a)

  class ProductOps(product: Product) {
    def plist[T] = product.productIterator.toList.asInstanceOf[List[T]]
  }

}

object ByteVectorConversions {
  implicit def byteVectorToByteArray(vec: ByteVector): Array[Byte] = vec.toArray

  implicit def byteArrayToByteVector(arr: Array[Byte]): ByteVector = ByteVector.view(arr)
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

object OptionConversions {
  implicit def optionToOptionOps[A](option: Option[A]): OptionOps[A] = new OptionOps(option)

  class OptionOps[A](option: Option[A]) {
    def either: FResult[A] =
      option.fold(InvalidArgumentError("Option is empty").left[A])(a => \/-(a))
  }

}

object KeyConversions {
  implicit def keyToKeyOps(key: Key): KeyOps = new KeyOps(key)

  class KeyOps(key: Key) {
    def getFingerprint: Array[Byte] = Crypto.hash(key.getEncoded, "SHA-1")
  }

}
