package edu.scalanus

import java.io.Reader
import javax.script._

class ScalanusScriptEngine private[scalanus](val factory: ScalanusScriptEngineFactory)
  extends AbstractScriptEngine with Invocable {

  override def eval(script: String, context: ScriptContext): AnyRef = ???

  override def eval(reader: Reader, context: ScriptContext): AnyRef = ???

  override def createBindings(): Bindings = new SimpleBindings()

  override def getFactory: ScriptEngineFactory = factory

  override def invokeMethod(thiz: scala.Any, name: String, args: AnyRef*): AnyRef = ???

  override def invokeFunction(name: String, args: AnyRef*): AnyRef = ???

  override def getInterface[T](returnType: Class[T]): T = ???

  override def getInterface[T](receiver: scala.Any, returnType: Class[T]): T = ???
}
