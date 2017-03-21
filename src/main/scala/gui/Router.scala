package gui

import javafx.scene.Parent

import gui.controllers.Redirect

import scalafx.beans.property.StringProperty

trait Router {
  val routes: Map[String, (Parent, Route)]

  val currentRoute: StringProperty

  def route = routes(currentRoute.value)

  def changeRoute(parent: Parent): Unit

  def redirect(path: String): Unit = {
    if (routes.contains(path)) {
      route._2.leave()

      val newRoute = routes(path)
      val res = newRoute._2.enter()

      res match {
        case Redirect(newPath) => redirect(newPath)
        case Continue() =>
          currentRoute.value = path
      }
    }
  }
}
