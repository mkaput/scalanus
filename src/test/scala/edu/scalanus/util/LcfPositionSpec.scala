package edu.scalanus.util

import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

class LcfPositionSpec extends FlatSpec with GivenWhenThen with Matchers {

  behavior of "LcfPosition"

  it should "generate nice toString" in {
    Given("only line number")
    LcfPosition(1).toString shouldBe "line 1"

    Given("line number and column number")
    LcfPosition(1, 1).toString shouldBe "line 1:1"

    Given("line number and file name")
    LcfPosition(1, fileName = "file.txt").toString shouldBe "file.txt:1"

    Given("line number, column number and file name")
    LcfPosition(1, 1, "file.txt").toString shouldBe "file.txt:1:1"
  }

  it should "throw IllegalArgumentException" in {
    Given("negative line number")
    an[IllegalArgumentException] should be thrownBy {
      LcfPosition(-1)
    }

    Given("column number less than -1")
    an[IllegalArgumentException] should be thrownBy {
      LcfPosition(10, -2)
    }
  }

}
