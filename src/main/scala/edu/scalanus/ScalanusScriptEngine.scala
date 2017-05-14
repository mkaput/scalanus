package edu.scalanus

import java.io.Reader
import javax.script._

class ScalanusScriptEngine private[scalanus](val factory: ScalanusScriptEngineFactory)
  extends AbstractScriptEngine with Compilable with Invocable {

  @throws[ScriptException]
  override def compile(script: String): CompiledScript = ???

  @throws[ScriptException]
  override def compile(script: Reader): CompiledScript = ???

  @throws[ScriptException]
  override def eval(script: String, context: ScriptContext): AnyRef = ???

  @throws[ScriptException]
  override def eval(reader: Reader, context: ScriptContext): AnyRef = ???

  override def createBindings(): Bindings = new SimpleBindings()

  override def getFactory: ScriptEngineFactory = factory

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeMethod(receiver: scala.Any, name: String, args: AnyRef*): AnyRef = ???

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeFunction(name: String, args: AnyRef*): AnyRef = ???

  override def getInterface[T](returnType: Class[T]): T = ???

  override def getInterface[T](receiver: scala.Any, returnType: Class[T]): T = ???
}
