package edu.scalanus.compiler

import javax.script.{ScriptContext, ScriptEngine}

import edu.scalanus.EngineTest
import edu.scalanus.errors.ScalanusParseException
import org.scalatest.{FlatSpec, Matchers}

class ScalanusCompilerSpec extends FlatSpec with Matchers with EngineTest {

  behavior of "Scalanus AST -> IR Compiler"

  it should "not fail on valid program" in {
    createEngine.compile("println(\"Hello World!\")")
  }

  it should "fail on invalid syntax with source name not provided" in {
    val ex = the[ScalanusParseException] thrownBy {
      createEngine.compile("println(\"Hello World!\"\u0000]")
    }
    ex.getMessage should not be empty
  }

  it should "fail on invalid syntax with source name provided" in {
    val ex2 = the[ScalanusParseException] thrownBy {
      val eng = createEngine
      eng.getContext.setAttribute(ScriptEngine.FILENAME, "foobar.scl", ScriptContext.ENGINE_SCOPE)
      eng.compile("println(\"Hello World!\"\u0000]")
    }
    ex2.getMessage should include("foobar.scl")
  }

}
