package edu.scalanus.compiler

import javax.script.{CompiledScript, ScriptContext, ScriptEngine}

class ScalanusCompiledScript(
  private val engine: ScriptEngine
) extends CompiledScript {

  override def getEngine: ScriptEngine = engine

  override def eval(context: ScriptContext): Any = ???

}
