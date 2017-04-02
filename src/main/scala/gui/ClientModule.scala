package gui

import java.util.{Locale, ResourceBundle}

import akka.actor.{ActorRef, ActorSystem}
import gui.internal.router.Router
import gui.internal.{FXMLViewLoader, ScaldiDependencyResolver, UTF8Control, ViewLoader}
import gui.model.{ChatsState, ClientState, State}
import gui.services.{ChatService, LoginService, MessageService}
import gui.view.model.{ChatsViewModel, LoginViewModel}
import network.ClientHandler
import network.ClientHandler.ClientListenerActor
import scaldi.Module
import scaldi.akka.AkkaInjectable

import scalafxml.core.ControllerDependencyResolver

class ClientModule extends Module {
  bind[ActorSystem] to ActorSystem("Client") destroyWith (_.terminate())

  binding toProvider new network.Client()
  binding toProvider new network.ClientHandler.ClientListenerActor()

  binding identifiedBy 'client to {
    implicit val system = inject[ActorSystem]

    AkkaInjectable.injectActorRef[network.Client]
  }

  binding identifiedBy 'clientListener to {
    implicit val system = inject[ActorSystem]

    AkkaInjectable.injectActorRef[ClientListenerActor]
  }

  bind[Locale] to new Locale("cs", "CZ")
  bind[ResourceBundle] to ResourceBundle.getBundle("Translations", inject[Locale], new UTF8Control)
  bind[ControllerDependencyResolver] to new ScaldiDependencyResolver()
  bind[ViewLoader] to injected[FXMLViewLoader]

  bind[State] to new State()
  bind[ClientState] to inject[State]
  bind[ChatsState] to inject[State]
  bind[Router] to injected[ClientRouter]
  bind[ClientHandler] to injected[ClientHandler]('client -> inject[ActorRef](identified by 'client), 'listenerActor -> inject[ActorRef](identified by 'clientListener))

  bind[LoginService] to injected[LoginService]
  bind[ChatService] to injected[ChatService]
  bind[MessageService] to injected[MessageService]

  bind[LoginViewModel] to injected[LoginViewModel]
  bind[ChatsViewModel] to injected[ChatsViewModel]

  println(inject[ChatsViewModel])
}
