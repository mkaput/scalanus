package edu.scalanus.compiler

import edu.scalanus.EngineTest
import edu.scalanus.errors.ScalanusParseException
import org.scalatest.{FlatSpec, Matchers}

class ScalanusCompilerSpec extends FlatSpec with Matchers with EngineTest {

  behavior of "Scalanus AST -> IR Compiler"

  it should "not fail on valid program" in {
    engine.compile("println(\"Hello World!\")")
  }

  it should "fail on invalid syntax" in {
    val ex = the[ScalanusParseException] thrownBy {
      engine.compile("println(\"Hello World!\"\0]")
    }
    ex.getMessage should not be empty
  }

}
