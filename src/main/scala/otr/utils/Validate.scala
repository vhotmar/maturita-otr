package otr.utils

import otr.ValidationError
import utils.Results.FResult

import scalaz.Scalaz._

class Validate(result: FResult[Boolean]) {
  def validate(message: String): FResult[Boolean] =
    result.fold(
      err => err.left,
      (res) => if (res) res.right else ValidationError(message).left
    )
}

object Validate {
  def apply(r: FResult[Boolean]) = new Validate(r)

  implicit def resultToValidate(r: FResult[Boolean]): Validate = Validate(r)

  implicit def booleanToValidate(r: Boolean): Validate = Validate(r.right)
}
