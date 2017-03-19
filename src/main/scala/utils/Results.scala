package utils
import scalaz.\/

object Results {

  case class None()

  case class Success()

  type FResult[A] = Throwable \/ A
  type SResult = FResult[Success]

  object FTry {
    def apply[T](a: => T): FResult[T] = {
      \/.fromTryCatchNonFatal(a)
    }
  }

}
