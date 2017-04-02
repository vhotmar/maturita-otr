package gui.services

class ClientService {

}

object ClientService {

  case class ClientIsNotInitialized() extends Throwable("Client is not initialized")

}
