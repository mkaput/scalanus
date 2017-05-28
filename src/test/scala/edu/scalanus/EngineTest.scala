package edu.scalanus

import javax.script.ScriptEngineManager

trait EngineTest {
  def engine: ScalanusScriptEngine =
    new ScriptEngineManager().getEngineByName("scalanus").asInstanceOf[ScalanusScriptEngine]
}
