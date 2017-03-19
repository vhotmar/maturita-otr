package network

import java.net.InetSocketAddress

trait ClientListener {
  def receivedMessage(id: Int, message: Array[Byte])

  def connectionClosed()

  def connectionFromUser(name: String, id: Int)

  def connectedToUser(name: String, id: Int)

  def userDoesNotExists(name: String)

  def disconnected()

  def disconnect()

  def sendingMessage(id: Int, message: Array[Byte])

  def connectingTo(name: String)

  def nameTaken(name: String)

  def registered(name: String, id: Int)

  def connectionFailed()

  def connected()

  def connecting(addr: InetSocketAddress)

}
