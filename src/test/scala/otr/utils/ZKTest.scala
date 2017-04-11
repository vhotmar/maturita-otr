package otr.utils

import org.scalatest.FunSuite
import otr.utils.SMP.ZK

class ZKTest extends FunSuite {

  test("testCheckLogProof") {
    val g = BigInt(2)
    val x: BigInt = 20

    val (c, d) = ZK.generateLogProof(g, x, 1)

    val y = g.modPow(x, SMP.ModulusS)

    assert(ZK.checkLogProof(c, d, g, y, 1))
  }

}
