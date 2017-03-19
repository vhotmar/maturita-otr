package otr.handlers


import java.security.PublicKey

import otr.utils.Crypto
import scodec.bits.ByteVector

object SMP {

  import otr.utils.KeyConversions._

  val ModulusS = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16)
  val ModulusMinus2 = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFD", 16)
  val OrderS = BigInt("7FFFFFFFFFFFFFFFE487ED5110B4611A62633145C06E0E68" + "948127044533E63A0105DF531D89CD9128A5043CC71A026E" + "F7CA8CD9E69D218D98158536F92F8A1BA7F09AB6B6A8E122" + "F242DABB312F3F637A262174D31BF6B585FFAE5B7A035BF6" + "F71C35FDAD44CFD2D74F9208BE258FF324943328F6722D9E" + "E1003E5C50B1DF82CC6D241B0E2AE9CD348B1FD47E9267AF" + "C1B2AE91EE51D6CB0E3179AB1042A95DCF6A9483B84B4B36" + "B3861AA7255E4C0278BA36046511B993FFFFFFFFFFFFFFFF", 16)
  val GeneratorS = BigInt(1, Array(0x02.toByte))

  def calculateSecret(localKey: PublicKey, remoteKey: PublicKey, ssid: Array[Byte], secret: Array[Byte]): Array[Byte] =
    (0x01.toByte +: localKey.getFingerprint) ++ remoteKey.getFingerprint ++ ssid ++ secret

  def proofKnowLog(g: BigInt, x: BigInt, version: Int): (BigInt, BigInt) = {
    val r = randomExponent()
    val tmp = g.modPow(r, ModulusS)
    val c = hashInt(version, tmp)
    val d = (r - (x * c).mod(OrderS)).mod(OrderS)

    (c, d)
  }

  def hashInt(version: Int, i: BigInt) =
    BigInt(1, hash(version, i))

  def hash(version: Int, i: BigInt): Array[Byte] =
    Crypto.hash(version.toByte +: writeBigInt(i))

  def writeBigInt(i: BigInt): Array[Byte] = {
    val bytes = i.toByteArray

    ByteVector.fromLong(bytes.length, 4).toArray ++ bytes
  }

  def randomExponent() = BigInt(1, Crypto.randomBytes(192))

  case class KeysState(local: PublicKey, remote: PublicKey, ssid: Array[Byte])

  case class BasicState(keys: KeysState, g1: BigInt, x2: BigInt, x3: BigInt)

  object BasicState {
    def apply(keys: KeysState): BasicState = new BasicState(keys, GeneratorS, randomExponent(), randomExponent())
  }


  /*
  case class NoSmpHandler(state: BasicState) extends Handler {
    override protected def process: Process = {
      case InitSMPAction(userSecret: Array[Byte], question: Option[Array[Byte]]) =>
        val secret = calculateSecret(state.keys.local, state.keys.remote, state.keys.ssid, userSecret)

        val s1: List[BigInt] = state.g1.modPow(state.x2, ModulusS) +: proofKnowLog(state.g1, state.x2, 1).toList
        val s2: List[BigInt] = state.g1.modPow(state.x3, ModulusS) +: proofKnowLog(state.g1, state.x3, 1).toList

      case SMP1Message()

    }
  }*/
}
