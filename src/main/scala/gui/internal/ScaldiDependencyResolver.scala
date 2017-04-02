package gui.internal

import scaldi.{Injectable, Injector, TypeTagIdentifier}
import utils.Results.FTry

import scala.reflect.runtime.universe._
import scalafxml.core.ControllerDependencyResolver

class ScaldiDependencyResolver(implicit val injector: Injector) extends ControllerDependencyResolver with Injectable {
  def get(paramName: String, dependencyType: Type): Option[Any] = {
    val identifiers = TypeTagIdentifier(dependencyType) :: Nil

    FTry(injectWithDefault[Any](injector, noBindingFound(identifiers))(identifiers)).toOption
  }
}
