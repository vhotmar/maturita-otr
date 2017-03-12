import java.security._
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider

// Add BouncyCastle provider to use ECDH implementation from BC
Security.addProvider(new BouncyCastleProvider)

// Use some secure curve
val ecSpec = ECNamedCurveTable.getParameterSpec("P-521")

val g = KeyPairGenerator.getInstance("ECDH", "BC")

// Create G - problem is that g should be constant by OTR specification
// TODO: add message to DH exchange
g.initialize(ecSpec, new SecureRandom())


// Generate keyPairs
val aKeyPair = g.generateKeyPair()
val bKeyPair = g.generateKeyPair()

// Create and initialize key agreements (class to generate secret)
val aKeyAgree = KeyAgreement.getInstance("ECDH", "BC")
aKeyAgree.init(aKeyPair.getPrivate)

val bKeyAgree = KeyAgreement.getInstance("ECDH", "BC")
bKeyAgree.init(bKeyPair.getPrivate)

// How is going to be public key decoded
val aDecPub = {
  val aEnc = aKeyPair.getPublic.getEncoded
  val ks = new X509EncodedKeySpec(aEnc)
  val kf = KeyFactory.getInstance("ECDH", "BC")
  kf.generatePublic(ks)
}

val bDecPub = {
  val bEnc = bKeyPair.getPublic.getEncoded
  val ks = new X509EncodedKeySpec(bEnc)
  val kf = KeyFactory.getInstance("ECDH", "BC")
  kf.generatePublic(ks)
}

// Use public key of other person to compute secret
aKeyAgree.doPhase(bDecPub, true)
bKeyAgree.doPhase(aDecPub, true)

val aSecret = aKeyAgree.generateSecret()
val bSecret = bKeyAgree.generateSecret()

val keySpec = new SecretKeySpec(aSecret, "AES/CTR")

keySpec.getFormat

aSecret.length

MessageDigest.isEqual(aSecret, bSecret)




