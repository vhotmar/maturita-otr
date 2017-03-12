package otr.utils

import java.security._
import java.security.interfaces.DSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Cipher, KeyAgreement, Mac}

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import otr.{FResult, FTry}
import scodec.bits.HexStringSyntax

object Crypto {
  val AESAlgorithm: String = "AES/CTR/NoPadding"
  val DSASignatureAlgorithm: String = "SHA1withDSA"
  val ZeroCtr: Array[Byte] = hex"0x0000000000000000000000000000".toArray
  val ECCurve: String = "P-521"

  def generateECKeyPair(): FResult[KeyPair] = FTry {
    val ecSpec = ECNamedCurveTable.getParameterSpec(ECCurve)

    val g = KeyPairGenerator.getInstance("ECDH", "BC")

    g.initialize(ecSpec, new SecureRandom())

    g.generateKeyPair()
  }

  def generateDSAKeyPair(): FResult[KeyPair] = FTry {
    val keyPairGenerator = KeyPairGenerator.getInstance("DSA")

    keyPairGenerator.initialize(1024, new SecureRandom())
    keyPairGenerator.generateKeyPair()
  }

  def getECSecret(privateKey: PrivateKey, publicKey: PublicKey): FResult[Array[Byte]] = FTry {
    val ecKeyAgreement = KeyAgreement.getInstance("ECDH", "BC")

    ecKeyAgreement.init(privateKey)
    ecKeyAgreement.doPhase(publicKey, true)

    ecKeyAgreement.generateSecret()
  }

  def encryptAES(bytes: Array[Byte], key: Array[Byte], ctr: Array[Byte] = ZeroCtr): FResult[Array[Byte]] =
    getAES(key, Cipher.ENCRYPT_MODE, ctr).flatMap(cipher => FTry(cipher.doFinal(bytes)))

  def getAES(key: Array[Byte], mode: Int, ctr: Array[Byte] = ZeroCtr): FResult[Cipher] = FTry {
    val cipher = Cipher.getInstance(AESAlgorithm, "BC")
    val keySpec = new SecretKeySpec(key, "AES")
    val ivSpec = new IvParameterSpec(ctr)

    cipher.init(mode, keySpec, ivSpec)

    cipher
  }

  def decryptAES(bytes: Array[Byte], key: Array[Byte], ctr: Array[Byte] = ZeroCtr): FResult[Array[Byte]] =
    getAES(key, Cipher.DECRYPT_MODE, ctr).flatMap(cipher => FTry(cipher.doFinal(bytes)))

  def parseECKey(bytes: Array[Byte]): FResult[ECPublicKey] = {
    for {
      publicKey <- parsePublicKey(bytes, "ECDH", Some("BC"))
      publicECKey <- FTry(publicKey.asInstanceOf[ECPublicKey])
    } yield publicECKey
  }

  def parsePublicKey(bytes: Array[Byte], algorithm: String, provider: Option[String] = None): FResult[PublicKey] = {
    for {
      keySpec <- FTry(new X509EncodedKeySpec(bytes))
      keyFactory <- FTry({
        provider match {
          case Some(name) => KeyFactory.getInstance(algorithm, name)
          case None => KeyFactory.getInstance(algorithm)
        }
      })
      publicKey <- FTry(keyFactory.generatePublic(keySpec))
    } yield publicKey
  }

  def parseDSAKey(bytes: Array[Byte]): FResult[DSAPublicKey] = {
    for {
      publicKey <- parsePublicKey(bytes, "DSA")
      publicDSAKey <- FTry(publicKey.asInstanceOf[DSAPublicKey])
    } yield publicDSAKey
  }

  def randomBytes(size: Int): Array[Byte] = {
    val random = new SecureRandom()
    val bytes: Array[Byte] = new Array[Byte](size)

    random.nextBytes(bytes)

    bytes
  }

  def verifyHash(hashArr: Array[Byte], bytes: Array[Byte]): Boolean =
    MessageDigest.isEqual(hashArr, hash(bytes))

  def hash(bytes: Array[Byte]): Array[Byte] = {
    val md = MessageDigest.getInstance("SHA-256")

    md.update(bytes)

    md.digest()
  }

  def verifyMac(key: Array[Byte], bytes: Array[Byte], v: Array[Byte]): FResult[Boolean] =
    hmac(bytes, key).map(mac => MessageDigest.isEqual(v, mac.take(v.length)))

  def hmac(bytes: Array[Byte], key: Array[Byte]): FResult[Array[Byte]] = FTry {
    val hmac = Mac.getInstance("HmacSHA256")
    val secretKeySpec = new SecretKeySpec(key, "HmacSHA256")

    hmac.init(secretKeySpec)

    hmac.doFinal(bytes)
  }

  def sign(bytes: Array[Byte], key: PrivateKey): FResult[Array[Byte]] = FTry {
    val signature = Signature.getInstance(DSASignatureAlgorithm)

    signature.initSign(key)
    signature.update(bytes)

    signature.sign()
  }

  def verify(bytes: Array[Byte], key: PublicKey): FResult[Boolean] = FTry {
    val signature = Signature.getInstance(DSASignatureAlgorithm)

    signature.initVerify(key)
    signature.verify(bytes)
  }

}
