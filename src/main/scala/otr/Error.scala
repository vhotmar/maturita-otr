package otr

import scodec.Err

class Error(message: String) extends Exception(message)

case class ParseError(error: Err) extends Error("Error while parsing")

case class ValidationError(message: String) extends Error(message)

case class InvalidArgumentError(message: String) extends Error(message)

