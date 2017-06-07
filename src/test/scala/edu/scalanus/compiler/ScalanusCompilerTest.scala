package edu.scalanus.compiler

import edu.scalanus.ir.IrTreePrettyPrinter
import edu.scalanus.{EngineTest, FileFixtureTest}
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class ScalanusCompilerTest extends FunSuite with Matchers with FileFixtureTest with EngineTest {

  override protected def basePath: String = "compiler/fixtures"

  fixtures foreach (f => {
    test(getFixtureName(f)) {
      val engine = createEngine
      val compiled = engine.compile(Source.fromFile(f).reader()).asInstanceOf[ScalanusCompiledScript]
      val actual = IrTreePrettyPrinter(compiled.ir)
      val results = getOrCreateResults(f, actual)
      actual shouldEqual results
    }
  })

}
