import otr.utils.Crypto
import scodec.bits.ByteVector


val ModulusS = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16)
val ModulusMinus2 = BigInt("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" + "83655D23DCA3AD961C62F356208552BB9ED529077096966D" + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFD", 16)
val OrderS = BigInt("7FFFFFFFFFFFFFFFE487ED5110B4611A62633145C06E0E68" + "948127044533E63A0105DF531D89CD9128A5043CC71A026E" + "F7CA8CD9E69D218D98158536F92F8A1BA7F09AB6B6A8E122" + "F242DABB312F3F637A262174D31BF6B585FFAE5B7A035BF6" + "F71C35FDAD44CFD2D74F9208BE258FF324943328F6722D9E" + "E1003E5C50B1DF82CC6D241B0E2AE9CD348B1FD47E9267AF" + "C1B2AE91EE51D6CB0E3179AB1042A95DCF6A9483B84B4B36" + "B3861AA7255E4C0278BA36046511B993FFFFFFFFFFFFFFFF", 16)
val GeneratorS = BigInt(1, Array(0x02.toByte))
val g: BigInt = 2
val x: BigInt = 20
val y: BigInt = 4
val (c, d) = proofKnowLog(g, x, 1)

def proofKnowLog(g: BigInt, x: BigInt, version: Int): (BigInt, BigInt) = {
  val r = randomExponent()
  val c = hashInt(version, g.modPow(r, ModulusS))
  val d = (r - (x * c)).mod(OrderS)

  (c, d)
}

def randomExponent() = BigInt(1, Crypto.randomBytes(192))

def hashInt(version: Int, i: BigInt) =
  BigInt(1, hash(version, i))

def hash(version: Int, i: BigInt): Array[Byte] =
  Crypto.hash(version.toByte +: writeBigInt(i))

def writeBigInt(i: BigInt): Array[Byte] = {
  val bytes = i.toByteArray

  ByteVector.fromLong(bytes.length, 4).toArray ++ bytes
}

def checkKnowLog(c: BigInt, d: BigInt, g: BigInt, x: BigInt, version: Int): Int = {
  val hc = hashInt(
    version,
    (g.modPow(d, ModulusS) * x.modPow(c, ModulusS)).mod(ModulusS)
  )

  hc.compare(c)
}

// def checkEqualCoords(g1: BigInt, g2: BigInt, g3: BigInt)

def proofEqualCoords(g1: BigInt, g2: BigInt, g3: BigInt, s: BigInt, r: BigInt, version: Int) = {
  val r1 = randomExponent()
  val r2 = randomExponent()

  val c = hashInt(version, g3.modPow(r1, ModulusS), (g1.modPow(r1, ModulusS) * g2.modPow(r2, ModulusS)).mod(ModulusS))

  val d1 = (r1 - (r * c)).mod(ModulusS)
  val d2 = (r2 - (s * c)).mod(ModulusS)

  (c, d1, d2)
}

def hashInt(version: Int, i1: BigInt, i2: BigInt) =
  BigInt(1, hash(version, i1, i2))

def hash(version: Int, i1: BigInt, i2: BigInt): Array[Byte] =
  Crypto.hash((version.toByte +: writeBigInt(i1)) ++ writeBigInt(i2))

def checkEqualCoords(c: BigInt, d1: BigInt, d2: BigInt, g1: BigInt, g2: BigInt, g3: BigInt, p: BigInt, q: BigInt, version: Int) = {
  val hc = hashInt(
    version,
    (g3.modPow(d1, ModulusS) * p.modPow(c, ModulusS)).mod(ModulusS),
    (q.modPow(c, ModulusS) * g1.modPow(d1, ModulusS) * g2.modPow(d2, ModulusS)).mod(ModulusS)
  )

  hc.compare(c)
}

g * x


checkKnowLog(c, d, g, g.modPow(x, ModulusS), 1)



