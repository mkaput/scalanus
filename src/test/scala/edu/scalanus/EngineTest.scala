package edu.scalanus

import javax.script.ScriptEngineManager

trait EngineTest {
  def createEngine: ScalanusScriptEngine =
    new ScriptEngineManager().getEngineByName("scalanus").asInstanceOf[ScalanusScriptEngine]
}
