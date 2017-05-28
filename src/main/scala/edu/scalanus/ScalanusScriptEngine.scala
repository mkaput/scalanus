package edu.scalanus

import java.io.Reader
import javax.script._

import edu.scalanus.compiler.ScalanusCompiler

class ScalanusScriptEngine private[scalanus](
  private val factory: ScalanusScriptEngineFactory
) extends AbstractScriptEngine with Compilable with Invocable {

  private val compiler = new ScalanusCompiler(this)

  @throws[ScriptException]
  override def compile(script: String): CompiledScript = try {
    compiler.compile(script, getScriptName(context))
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def compile(script: Reader): CompiledScript = try {
    compiler.compile(script, getScriptName(context))
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def eval(script: String, context: ScriptContext): AnyRef = try {
    compile(script).eval(context)
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def eval(script: Reader, context: ScriptContext): AnyRef = try {
    compile(script).eval(context)
  } catch {
    case e: ScriptException => throw e
    case e: ScalanusException => throw e.intoScriptException()
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
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

  private def getScriptName(ctx: ScriptContext): String =
    Option(ctx.getAttribute(ScriptEngine.FILENAME))
      .map(_.toString)
      .getOrElse("<eval>")

}
