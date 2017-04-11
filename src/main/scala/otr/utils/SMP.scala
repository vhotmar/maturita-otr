package otr.utils

import java.security.PublicKey

import scodec.bits.ByteVector
import scodec.{Attempt, Codec}
import utils.Results.FTry

object SMP {

  import otr.utils.KeyConversions._

  val ModulusS = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16)
  val ModulusMinus2 = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFD", 16)
  val OrderS = BigInt("7FFFFFFFFFFFFFFFE487ED5110B4611A62633145C06E0E68" + "948127044533E63A0105DF531D89CD9128A5043CC71A026E" + "F7CA8CD9E69D218D98158536F92F8A1BA7F09AB6B6A8E122" + "F242DABB312F3F637A262174D31BF6B585FFAE5B7A035BF6" + "F71C35FDAD44CFD2D74F9208BE258FF324943328F6722D9E" + "E1003E5C50B1DF82CC6D241B0E2AE9CD348B1FD47E9267AF" + "C1B2AE91EE51D6CB0E3179AB1042A95DCF6A9483B84B4B36" + "B3861AA7255E4C0278BA36046511B993FFFFFFFFFFFFFFFF", 16)
  val GeneratorS = BigInt(1, Array(0x02.toByte))

  def randomExponent(): BigInt = BigInt(1, Crypto.randomBytes(192)).mod(ModulusS)

  def hashInt(prefix: Int, bint: BigInt, bints: BigInt*): BigInt =
    BigInt(1, hash(prefix, bint, bints: _*)) // bints need to be explicitly typed as varargs

  def hash(prefix: Int, bint: BigInt, bints: BigInt*): Array[Byte] = {
    val bytes = (bint :: bints.toList).flatMap(writeBigInt).toArray

    Crypto.hash(ByteVector.fromInt(prefix).toArray ++ bytes)
  }

  def writeBigInt(i: BigInt): Array[Byte] = {
    val bytes = i.toByteArray

    ByteVector.fromLong(bytes.length, 4).toArray ++ bytes
  }

  def bigIntCodec: Codec[BigInt] = {
    import otr.utils.ByteVectorConversions._
    import scodec.codecs._

    variableSizeBytes(
      int32,
      bytes.exmap(
        x =>
          Attempt.fromEither(
            FTry(BigInt(x))
              .leftMap(x => scodec.Err("error while parsing BigInt"))
              .toEither
          ),
        x => Attempt.successful(x.toByteArray)
      )
    )
  }

  def check(x: BigInt): Boolean = checkGroupElem(x) || checkExpon(x)

  def checkGroupElem(x: BigInt): Boolean = x.compare(BigInt(2)) > 0 || x.compare(ModulusMinus2) < 0

  def checkExpon(x: BigInt): Boolean = x.compare(BigInt(1)) > 0 || x.compare(ModulusS) <= 0

  def calculateSecret(localKey: PublicKey, remoteKey: PublicKey, ssid: Array[Byte], secret: Array[Byte]): BigInt =
    BigInt(1, (0x01.toByte +: localKey.getFingerprint) ++ remoteKey.getFingerprint ++ ssid ++ secret)

  object State {

    case class Keys(local: PublicKey, remote: PublicKey, ssid: Array[Byte])

    case class Step1State(keys: Keys, secret: BigInt, g1: BigInt, x2: BigInt, x3: BigInt)

    case class Step1AState(keys: Keys, g1: BigInt, g2: BigInt, g3: BigInt, g3o: BigInt, x2: BigInt, x3: BigInt)

    case class Step1BState(keys: Keys, secret: BigInt, g1: BigInt, g2: BigInt, g3: BigInt, g3o: BigInt, x2: BigInt, x3: BigInt, p: BigInt, q: BigInt)

    case class Step2State(keys: Keys, secret: BigInt, g1: BigInt, g2: BigInt, g3: BigInt, g3o: BigInt, x2: BigInt, x3: BigInt, p: BigInt, q: BigInt, pab: BigInt, qab: BigInt, ra: BigInt)

    object Step1State {
      def apply(keys: Keys, secret: Array[Byte]): Step1State =
        new Step1State(
          keys,
          calculateSecret(keys.local, keys.remote, keys.ssid, secret),
          GeneratorS,
          randomExponent(),
          randomExponent()
        )
    }

    object Step1AState {

      def apply(keys: Keys, g1: BigInt, g2a: BigInt, g3a: BigInt): Step1AState = {
        val x2 = randomExponent()
        val x3 = randomExponent()

        new Step1AState(keys, g1, g2a.modPow(x2, ModulusS), g3a.modPow(x3, ModulusS), g3a, x2, x3)
      }
    }

    object Step1BState {
      def apply(state: Step1AState, secret: Array[Byte], r: BigInt): Step1BState = {
        val computedSecret =
          calculateSecret(state.keys.remote, state.keys.local, state.keys.ssid, secret)

        new Step1BState(
          state.keys,
          computedSecret,
          state.g1,
          state.g2,
          state.g3,
          state.g3o,
          state.x2,
          state.x3,
          state.g3.modPow(r, ModulusS),
          (state.g1.modPow(r, ModulusS) * state.g2.modPow(computedSecret, ModulusS)).mod(ModulusS)
        )
      }
    }

    object Step2State {
      def apply(state: Step1State, g2o: BigInt, g3o: BigInt, po: BigInt, qo: BigInt, r: BigInt): Step2State = {
        val g2 = g2o.modPow(state.x2, ModulusS)
        val g3 = g3o.modPow(state.x3, ModulusS)
        val p = g3.modPow(r, ModulusS)
        val q = (state.g1.modPow(r, ModulusS) * g2.modPow(state.secret, ModulusS)).mod(ModulusS)
        val qab = (q * qo.modInverse(ModulusS)).mod(ModulusS)

        Step2State(
          state.keys,
          state.secret,
          state.g1,
          g2,
          g3,
          g3o,
          state.x2,
          state.x3,
          p,
          q,
          (p * po.modInverse(ModulusS)).mod(ModulusS),
          qab,
          qab.modPow(state.x3, ModulusS)
        )
      }
    }

  }

  object ZK {
    /**
      * Returns zero-knowledge proof, that we knows x from g^x^ % m
      *
      * {{{
      * c = H(p, (g ^ r) % m)
      * d = (r - x * c) % s
      * }}}
      *
      * @param g generator
      * @param x discrete logarithm
      * @param p prefix
      * @return (c, d)
      */
    def generateLogProof(g: BigInt, x: BigInt, p: Int): (BigInt, BigInt) = {
      val r = randomExponent()

      val c = hashInt(p, g.modPow(r, ModulusS))
      val d = (r - (x * c)).mod(OrderS)

      (c, d)
    }

    /**
      * Checks for c == Hash(p, (g^d^ * y^c^) % m)
      *
      * {{{
      * g ^ r == g ^ d * y ^ c
      * g ^ r == g ^ (r - x * c) * (g ^ x) ^ c
      * g ^ r == g ^ (r - x * c) * g ^ (x * c)
      * g ^ r == g ^ (r - x * c + x * c)
      * g ^ r == g ^ r
      * }}}
      *
      * @param c received C
      * @param d received D
      * @param g generator
      * @param y received (g &#94; x) % m
      * @param p prefix
      * @return
      */
    def checkLogProof(c: BigInt, d: BigInt, g: BigInt, y: BigInt, p: Int): Boolean = {
      val t = hashInt(p, (g.modPow(d, ModulusS) * y.modPow(c, ModulusS)).mod(ModulusS))

      t.compare(c) == 0
    }

    /**
      * Generate proof for g2, g3, secret and r
      *
      * @param g1     generator
      * @param g2     (g1 &#94; y2) &#94; x2
      * @param g3     (g1 &#94; y3) &#94; x3
      * @param secret secret to check
      * @param r      r to check
      * @param p      prefix
      * @return
      */
    def generateLogCoordsProof(g1: BigInt, g2: BigInt, g3: BigInt, secret: BigInt, r: BigInt, p: Int): (BigInt, BigInt, BigInt) = {
      val r1 = randomExponent()
      val r2 = randomExponent()

      val c = hashInt(p, g3.modPow(r1, ModulusS), (g1.modPow(r1, ModulusS) * g2.modPow(r2, ModulusS)).mod(ModulusS))

      val d1 = (r1 - (r * c)).mod(OrderS)
      val d2 = (r2 - (secret * c)).mod(OrderS)

      (c, d1, d2)
    }

    /**
      *
      * @param c  (remote) c
      * @param d1 (remote) d1
      * @param d2 (remote) d2
      * @param g1 g1
      * @param g2 (g1 &#94; x2) &#94; y2
      * @param g3 (g1 &#94; x3) &#94; y3
      * @param q  (remote) g3 &#94; r
      * @param p  (remote) g1 &#94; r * g2 &#94; secret
      * @param pr prefix
      * @return
      */
    def checkLogCoordsProof(c: BigInt, d1: BigInt, d2: BigInt, g1: BigInt, g2: BigInt, g3: BigInt, q: BigInt, p: BigInt, pr: Int): Boolean = {
      val cp = hashInt(
        pr,
        (g3.modPow(d1, ModulusS) * p.modPow(c, ModulusS)).mod(ModulusS),
        (g1.modPow(d1, ModulusS) * g2.modPow(d2, ModulusS) * q.modPow(c, ModulusS)).mod(ModulusS)
      )

      cp.compare(c) == 0
    }

    def generateLogEqualProof(g1: BigInt, x3: BigInt, qab: BigInt, pr: Int): (BigInt, BigInt) = {
      val r = randomExponent()

      val c = hashInt(pr, g1.modPow(r, ModulusS), qab.modPow(r, ModulusS))
      val d = (r - x3 * c).mod(OrderS)

      (c, d)
    }

    def checkLogEqualProof(c: BigInt, d: BigInt, g1: BigInt, g3o: BigInt, qab: BigInt, r: BigInt, pr: Int): Boolean = {
      val cp = hashInt(
        pr,
        (g1.modPow(d, ModulusS) * g3o.modPow(c, ModulusS)).mod(ModulusS),
        (qab.modPow(d, ModulusS) * r.modPow(c, ModulusS)).mod(ModulusS)
      )

      cp.compare(c) == 0
    }
  }

}
