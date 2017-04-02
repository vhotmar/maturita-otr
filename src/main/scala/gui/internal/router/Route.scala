package gui.internal.router

import scalafx.stage.Stage

trait Route {
  def enter(stage: Stage): RouteResult = Continue()

  def leave(): Unit = {}
}
