
import scalaz.Scalaz._

val e = Map(
  "x" -> "X".success,
  "y" -> "D".failure
)

val d = e.filter(_._2.isFailure)