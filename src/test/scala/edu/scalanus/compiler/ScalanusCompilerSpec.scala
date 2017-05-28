package edu.scalanus.compiler

import javax.script.{ScriptContext, ScriptEngine}

import edu.scalanus.EngineTest
import edu.scalanus.errors.ScalanusParseException
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

class ScalanusCompilerSpec extends FlatSpec with GivenWhenThen with Matchers with EngineTest {

  behavior of "Scalanus AST -> IR Compiler"

  it should "not fail on valid program" in {
    createEngine.compile("println(\"Hello World!\")")
  }

  it should "fail on invalid syntax" in {
    When("source name wasn't provided")
    val ex = the[ScalanusParseException] thrownBy {
      createEngine.compile("println(\"Hello World!\"\0]")
    }
    ex.getMessage should not be empty

    Given("source name")
    val ex2 = the[ScalanusParseException] thrownBy {
      val eng = createEngine
      eng.getContext.setAttribute(ScriptEngine.FILENAME, "foobar.scl", ScriptContext.ENGINE_SCOPE)
      eng.compile("println(\"Hello World!\"\0]")
    }
    ex2.getMessage should include("foobar.scl")
  }

}
