package gui.internal.router

abstract class RouteResult {}

case class Continue() extends RouteResult

case class Redirect(path: String) extends RouteResult
