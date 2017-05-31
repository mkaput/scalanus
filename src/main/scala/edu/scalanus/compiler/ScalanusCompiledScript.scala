package edu.scalanus.compiler

import javax.script.{CompiledScript, ScriptContext, ScriptEngine}

import edu.scalanus.ir.IrNode

class ScalanusCompiledScript(
  val ir: IrNode,
  private val engine: ScriptEngine
) extends CompiledScript {

  override def getEngine: ScriptEngine = engine

  override def eval(context: ScriptContext): AnyRef = ???

}
