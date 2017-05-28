package edu.scalanus.util

import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

class LocationTest extends FlatSpec with GivenWhenThen with Matchers {

  "Location" should "generate nice toString" in {
    Given("only line number")
    Location(1).toString shouldBe "line 1"

    Given("line number and column number")
    Location(1, 1).toString shouldBe "line 1:1"

    Given("line number and file name")
    Location(1, fileName = "file.txt").toString shouldBe "file.txt:1"

    Given("line number, column number and file name")
    Location(1, 1, "file.txt").toString shouldBe "file.txt:1:1"
  }

  it should "throw IllegalArgumentException" in {
    Given("negative line number")
    a[IllegalArgumentException] should be thrownBy {
      Location(-1)
    }

    Given("column number less than -1")
    a[IllegalArgumentException] should be thrownBy {
      Location(10, -2)
    }
  }

}
