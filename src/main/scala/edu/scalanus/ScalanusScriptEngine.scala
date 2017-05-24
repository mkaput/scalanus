package edu.scalanus

import java.io.Reader
import javax.script._

class ScalanusScriptEngine private[scalanus](
  private val factory: ScalanusScriptEngineFactory
) extends AbstractScriptEngine with Compilable with Invocable {

  @throws[ScriptException]
  override def compile(script: String): CompiledScript = throwingScriptException {
    ???
  }

  @throws[ScriptException]
  override def compile(script: Reader): CompiledScript = throwingScriptException {
    ???
  }

  @throws[ScriptException]
  override def eval(script: String, context: ScriptContext): AnyRef = throwingScriptException {
    ???
  }

  @throws[ScriptException]
  override def eval(reader: Reader, context: ScriptContext): AnyRef = throwingScriptException {
    ???
  }

  override def createBindings(): Bindings = new SimpleBindings()

  override def getFactory: ScriptEngineFactory = factory

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeMethod(receiver: scala.Any, name: String, args: AnyRef*): AnyRef = try {
    ???
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: NoSuchElementException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeFunction(name: String, args: AnyRef*): AnyRef = try {
    ???
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: NoSuchElementException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  override def getInterface[T](returnType: Class[T]): T = ???

  override def getInterface[T](receiver: scala.Any, returnType: Class[T]): T = ???

  private def throwingScriptException[T](f: () => T): T =
    try {
      f()
    } catch {
      case e: ScriptException => throw e
      case e: ScalanusException => throw e.intoScriptException()
      case e: RuntimeException => throw e
      case e: Exception => throw new ScriptException(e)
    }

}
