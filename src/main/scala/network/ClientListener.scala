package network

import java.net.InetSocketAddress

trait ClientListener {
  def receivedMessage(id: Int, message: Array[Byte]): Unit = {}

  def connectionClosed(): Unit = {}

  def connectionFromUser(name: String, id: Int): Unit = {}

  def connectedToUser(name: String, id: Int): Unit = {}

  def userDoesNotExists(name: String): Unit = {}

  def disconnected(): Unit = {}

  def disconnect(): Unit = {}

  def sendingMessage(id: Int, message: Array[Byte]): Unit = {}

  def connectingTo(name: String): Unit = {}

  def nameTaken(name: String): Unit = {}

  def registered(name: String, id: Int): Unit = {}

  def connectionFailed(): Unit = {}

  def connected(): Unit = {}

  def connecting(addr: InetSocketAddress): Unit = {}

}
