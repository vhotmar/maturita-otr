package otr.utils

import org.scalatest.EitherValues._
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class Crypto$Test extends FunSuite {

  test("testVerifyMac") {
    val key = Crypto.randomBytes(16)
    val data = Crypto.randomBytes(128)

    val mac = Crypto.hmac(data, key)

    val verification = for {
      mac <- Crypto.hmac(data, key)
      verify <- Crypto.verifyMac(data, key, mac)
    } yield verify

    verification.toEither.right.value should be(true)
  }

  test("testVerifyMac with arbitary mac length") {
    val key = Crypto.randomBytes(16)
    val data = Crypto.randomBytes(128)

    val mac = Crypto.hmac(data, key)

    val verification = for {
      mac <- Crypto.hmac(data, key)
      verify <- Crypto.verifyMac(data, key, mac.take(20))
    } yield verify

    verification.toEither.right.value should be(true)
  }

}
