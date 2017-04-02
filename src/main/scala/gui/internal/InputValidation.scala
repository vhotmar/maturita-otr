package gui.internal

import scalaz.{Bind, Kleisli, Validation, ValidationNel}

object InputValidation {
  type Va[A] = Validation[String, A]
  type VaNel[A] = ValidationNel[String, A]

  type Vali[A, B] = Kleisli[Va, A, B]

  /** Wraps a validator in Kleisli so that it could be piped from/into another Kleisli wrapped validator */
  def Vali[In, Out](fn: In => Va[Out]): Vali[In, Out] = Kleisli[Va, In, Out](fn)

  // for Kleisli and its >=>
  implicit val vaBinding = new Bind[Va] {
    def map[A, B](fa: Va[A])(f: A => B): Va[B] = fa.map(f)

    def bind[A, B](fa: Va[A])(f: A => Va[B]): Va[B] = {
      import scalaz.Validation.FlatMap._
      fa.flatMap(f)
    }
  }
}