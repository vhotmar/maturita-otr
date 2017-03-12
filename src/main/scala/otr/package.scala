import scalaz.\/

package object otr {
  type FResult[A] = Throwable \/ A

  object FTry {
    def apply[T](a: => T): FResult[T] = {
      \/.fromTryCatchNonFatal(a)
    }
  }

}
