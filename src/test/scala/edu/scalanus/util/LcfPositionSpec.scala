package edu.scalanus.util

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class LcfPositionSpec extends FlatSpec with TableDrivenPropertyChecks with Matchers {

  behavior of "LcfPosition"

  it should "generate nice toString" in {
    val data = Table(
      ("position", "toString"),
      (LcfPosition(1), "line 1"),
      (LcfPosition(1, 1), "line 1:1"),
      (LcfPosition(1, fileName = "file.txt"), "file.txt:1"),
      (LcfPosition(1, 1, "file.txt"), "file.txt:1:1")
    )

    forEvery(data) { (pos, str) =>
      pos.toString shouldBe str
    }
  }

  it should "throw IllegalArgumentException given negative line number" in {
    an[IllegalArgumentException] should be thrownBy {
      LcfPosition(-1)
    }
  }

  it should "throw IllegalArgumentException given column number less than -1" in {
    an[IllegalArgumentException] should be thrownBy {
      LcfPosition(10, -2)
    }
  }

}
