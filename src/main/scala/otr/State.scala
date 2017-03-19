package otr

import java.security.{KeyPair, PrivateKey, PublicKey}

import _root_.utils.Results.FResult
import otr.utils.Crypto

trait PartialPublic {
  val publicKey: PublicKey
}

trait Public extends PartialPublic {
  val longTermPublicKey: PublicKey
  val keyId: Int
}

trait Private {
  val privateKey: PrivateKey
  val longTermPrivateKey: PrivateKey
}

trait Pair extends Public with Private {}

case class Local(
  publicKey: PublicKey,
  privateKey: PrivateKey,
  longTermPublicKey: PublicKey,
  longTermPrivateKey: PrivateKey,
  keyId: Int
) extends Pair

case class NonCompleteRemote(publicKey: PublicKey) extends PartialPublic

case class Remote(publicKey: PublicKey, longTermPublicKey: PublicKey, keyId: Int) extends Public

case class Parameters(
  secret: Array[Byte],
  ssid: Array[Byte],
  c: Array[Byte],
  cp: Array[Byte],
  m1: Array[Byte],
  m1p: Array[Byte],
  m2: Array[Byte],
  m2p: Array[Byte]
)


object Parameters {
  def apply(secret: Array[Byte]): Parameters = {
    // this little bit differs from original implementation, because our secret
    // is already encoded (probably in different format then MPI - because EC works
    // with different format
    def h2(byte: Byte): Array[Byte] =
    Crypto.hash(byte +: secret)

    val ssid = h2(0x00).take(8)
    val (c, cp) = h2(0x01).splitAt(16)
    val m1 = h2(0x02)
    val m1p = h2(0x03)
    val m2 = h2(0x04)
    val m2p = h2(0x05)

    new Parameters(secret, ssid, c, cp, m1, m1p, m2, m2p)
  }
}


case class NonCompleteState(local: Local, remote: NonCompleteRemote, parameters: Parameters)

object NonCompleteState {
  def create(localKeyPair: KeyPair, remotePublicKey: PublicKey, longTermKeyPair: KeyPair): FResult[NonCompleteState] = {
    val secret = Crypto.getECSecret(localKeyPair.getPrivate, remotePublicKey)

    secret.map(s =>
      new NonCompleteState(
        Local(
          localKeyPair.getPublic,
          localKeyPair.getPrivate,
          longTermKeyPair.getPublic,
          longTermKeyPair.getPrivate,
          1
        ),
        NonCompleteRemote(remotePublicKey),
        Parameters(s)
      )
    )
  }
}

case class State(local: Local, remote: Remote, parameters: Parameters)


object State {
  def apply(state: NonCompleteState, remoteLongTermPublicKey: PublicKey, remoteKeyId: Int): State = {
    State(
      state.local,
      Remote(state.remote.publicKey, remoteLongTermPublicKey, remoteKeyId),
      state.parameters
    )
  }
}