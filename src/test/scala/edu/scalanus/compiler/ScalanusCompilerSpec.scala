package edu.scalanus.compiler

import javax.script.{ScriptContext, ScriptEngine, ScriptException}

import edu.scalanus.EngineTest
import org.scalatest.{FlatSpec, Matchers}

class ScalanusCompilerSpec extends FlatSpec with Matchers with EngineTest {

  behavior of "Scalanus AST -> IR Compiler"

  it should "not fail on valid program" in {
    // This funny program source comes from the
    // iterating nature of compiler development ;)
    createEngine.compile("1; 2; 3; 4")
  }

  it should "fail on invalid syntax with source name not provided" in {
    val ex = the[ScriptException] thrownBy {
      createEngine.compile("println(\"Hello World!\"\u0000]")
    }
    ex.getMessage should not be empty
  }

  it should "fail on invalid syntax with source name provided" in {
    val ex = the[ScriptException] thrownBy {
      val eng = createEngine
      eng.getContext.setAttribute(ScriptEngine.FILENAME, "foobar.scl", ScriptContext.ENGINE_SCOPE)
      eng.compile("println(\"Hello World!\"\u0000]")
    }
    ex.getMessage should include("foobar.scl")
  }


  behavior of "Scalanus AST -> IR Compiler Errors"

  it should "raise \"expected reference expression\" when trying to increment non reference expression" in {
    val ex = the[ScriptException] thrownBy {
      createEngine.compile("++5")
    }

    ex.getMessage should include("reference")
  }

}
