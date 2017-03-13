package otr.state

import java.security.{KeyPair, PublicKey}

import otr.messages.Data
import otr.messages.data.DataT
import otr.messages.types.{Encrypted, Mac}
import otr.utils.Crypto
import otr.{FResult, FTry, State}

case class DataState(
  localLongTermKeyPair: KeyPair,
  remoteLongTermPublicKey: PublicKey,
  localDHKeyPairs: Map[Int, KeyPair],
  remoteDHPublicKey: (Int, PublicKey),
  counter: Array[Byte]
) {

  private def h1(secret: Array[Byte], byte: Byte): Array[Byte] =
    Crypto.hash(byte +: secret, "SHA-1")

  private def generateSendingKeys(localKeyId: Int, remoteKeyId: Int, secret: Array[Byte]): (Array[Byte], Array[Byte]) = {
    // TODO: may cause problems
    val byte: Byte = if (localKeyId > remoteKeyId || localKeyId == remoteKeyId) 0x01 else 0x02

    val aes = h1(secret, byte).take(16)
    val mac = Crypto.hash(aes, "SHA-1").take(20)

    (aes, mac)
  }

  private def generateReceivingKeys(localKeyId: Int, remoteKeyId: Int, secret: Array[Byte]): (Array[Byte], Array[Byte]) = {
    val byte: Byte = if (localKeyId > remoteKeyId && localKeyId != remoteKeyId) 0x02 else 0x01

    val aes = h1(secret, byte).take(16)
    val mac = Crypto.hash(aes, "SHA-1").take(20)

    (aes, mac)
  }

  private def increaseCounter(counter: Array[Byte]): Array[Byte] = {
    // increase counter as specified in otr4j
    val cloned = counter.clone()

    for (i <- 7 to 0) {
      val c = cloned(i)

      cloned(i) = (c + 1).toByte

      if (c == 0)
        return cloned
    }

    cloned
  }

  def sendMessage(message: Array[Byte]): FResult[(DataState, Data)] = {
    import otr.utils.ByteVectorConversions._
    for {
    // get latest key
      localResult <- FTry(localDHKeyPairs.maxBy(_._1))
      (localKeyId, localKey) = localResult

      // generate new key and keyId
      newLocal <- Crypto.generateECKeyPair()
      newLocalId = localKeyId + 1

      // generate aes and mac keys
      secret <- Crypto.getECSecret(localKey.getPrivate, remoteDHPublicKey._2)
      (aesKey, macKey) = generateSendingKeys(localKeyId, remoteDHPublicKey._1, secret)

      // generate new state (just add the new local key and increase counter)
      newState = copy(
        localDHKeyPairs = localDHKeyPairs + (newLocalId -> newLocal),
        counter = increaseCounter(counter)
      )

      // encrypt message to be sent
      encryptedMessage <- Crypto.encryptAES(message, aesKey, newState.counter)

      // initialize DataT part of the message
      dataT = DataT(localKeyId, remoteDHPublicKey._1, newLocal.getPublic, newState.counter, Encrypted(encryptedMessage))
      encodedDataT <- dataT.encode.map(_.bytes)

      // create mac of the DataT
      mac <- Mac.create(encodedDataT, macKey, 20)

      // create message
      message = Data(0x00, otr.messages.types.Data(Some(encodedDataT), dataT), mac)
    } yield (newState, message)
  }

  def receiveMessage(message: Data): FResult[(DataState, Array[Byte])] = {
    import otr.utils.ByteVectorConversions._
    import otr.utils.OptionConversions._
    import otr.utils.Validate._

    val dataT = message.dataT.value
    for {
    // check if exist requested receiver key id
      _ <- localDHKeyPairs.exists(_._1 == dataT.receiverKeyId).validate("Don't have needed key")
      localResult <- localDHKeyPairs.find(_._1 == dataT.receiverKeyId).either
      (localKeyId, localKey) = localResult

      // check if used latest provided key - should not happen
      _ <- (dataT.senderKeyId == remoteDHPublicKey._1).validate("Didn't use latest key")

      // generate secrets
      secret <- Crypto.getECSecret(localKey.getPrivate, remoteDHPublicKey._2)
      (aesKey, macKey) = generateReceivingKeys(localKeyId, remoteDHPublicKey._1, secret)

      // verify mac signature of data message
      dataTBytes <- message.dataT.bytes.either
      _ <- message.mac.verify(dataTBytes, macKey).validate("Invalid mac signature")

      decryptedMessage <- dataT.encrypted.decrypt(aesKey, dataT.counter)

      newState = copy(
        // replace their public key with newer one
        remoteDHPublicKey = (dataT.senderKeyId + 1) -> dataT.nextPublicKey,
        // remove keys, which are the other side not going to use
        localDHKeyPairs = localDHKeyPairs.filter(x => x._1 >= dataT.receiverKeyId)
      )
    } yield (newState, decryptedMessage)
  }
}

object DataState {
  def apply(state: State): DataState = new DataState(
    new KeyPair(state.local.longTermPublicKey, state.local.longTermPrivateKey),
    state.remote.longTermPublicKey,
    Map(state.local.keyId -> new KeyPair(state.local.publicKey, state.local.privateKey)),
    state.remote.keyId -> state.remote.publicKey,
    Crypto.ZeroCtr
  )
}
