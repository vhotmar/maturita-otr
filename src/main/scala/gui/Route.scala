package gui

trait Route {
  def enter(): RouteResult = Continue()
  def leave(): Unit = {}
}

class RouteResult
case class Continue() extends RouteResult
case class Redirect(url: String) extends RouteResult
