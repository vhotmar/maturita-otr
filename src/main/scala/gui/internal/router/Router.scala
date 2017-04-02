package gui.internal.router

import javafx.scene.{Parent, Scene}

import scalafx.stage.Stage

abstract class Router extends Scene(new javafx.scene.Group) {
  protected var currentRoute: String
  protected var stage: Stage = _
  private var routes: Map[String, (Parent, Route)] = _

  def redirect(path: String): Unit = {
    if (routes.contains(path)) {
      val (_, route) = routes(currentRoute)

      route.leave()

      val (newParent, newRoute) = routes(path)
      val res = newRoute.enter(stage)

      res match {
        case Redirect(newPath) => redirect(newPath)
        case Continue() =>
          currentRoute = path

          changeRoute(newParent)
      }
    }
  }

  def initRoutes(): Map[String, (Parent, Route)]

  def init(_stage: Stage): Unit = {
    stage = _stage
    routes = initRoutes()

    redirect(currentRoute)
  }

  protected def changeRoute(parent: Parent): Unit = {
    setRoot(parent)
  }
}
