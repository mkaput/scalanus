package edu.scalanus

import java.io.Reader
import javax.script._

import edu.scalanus.compiler.ScalanusCompiler
import edu.scalanus.errors.ScalanusException
import edu.scalanus.interpreter.{ScalanusInterpreter, ScalanusMethod, ScalanusScriptContext}
import edu.scalanus.ir.{IrCtx, IrFnCallExpr, IrValue}
import edu.scalanus.stdlib.ScalanusStdLib
import edu.scalanus.util.LcfPosition

class ScalanusScriptEngine private[scalanus](
  private val factory: ScalanusScriptEngineFactory
) extends AbstractScriptEngine with Compilable with Invocable {

  private val compiler = new ScalanusCompiler(this)
  context = new ScalanusScriptContext(context) // override default context
  ScalanusStdLib.addStdLib(context.asInstanceOf[ScalanusScriptContext])

  @throws[ScriptException]
  override def compile(script: String): CompiledScript = try {
    compiler.compile(script, getScriptName(context))
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def compile(script: Reader): CompiledScript = try {
    compiler.compile(script, getScriptName(context))
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def eval(script: String, context: ScriptContext): AnyRef = try {
    compile(script).eval(context)
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  override def eval(script: Reader, context: ScriptContext): AnyRef = try {
    compile(script).eval(context)
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  override def createBindings(): Bindings = new SimpleBindings()

  override def getFactory: ScriptEngineFactory = factory

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeMethod(receiver: Any, name: String, args: AnyRef*): AnyRef = try {
    val scalanusScriptContext = context.asInstanceOf[ScalanusScriptContext]
    ScalanusMethod(receiver, name)
      .eval(args, scalanusScriptContext, scalanusScriptContext.PROGRAM_SCOPE)
      .asInstanceOf[AnyRef]
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: NoSuchElementException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  @throws[ScriptException]
  @throws[NoSuchMethodException]
  override def invokeFunction(name: String, args: AnyRef*): AnyRef = try {
    val ctx = IrCtx(LcfPosition(0))
    val node = IrFnCallExpr(IrValue(name)(ctx), args.map(arg=> IrValue(arg)(ctx)).toIndexedSeq)(ctx)
    ScalanusInterpreter.eval(node, context)
  } catch {
    case e: ScalanusException => throw e.toScriptException
    case e: ScriptException => throw e
    case e: NoSuchElementException => throw e
    case e: RuntimeException => throw e
    case e: Exception => throw new ScriptException(e)
  }

  override def getInterface[T](returnType: Class[T]): T = ??? // No idea

  override def getInterface[T](receiver: Any, returnType: Class[T]): T = ??? // No idea

  private def getScriptName(ctx: ScriptContext): String = {
    val attr = ctx.getAttribute(ScriptEngine.FILENAME)
    if (attr == null) null else attr.toString
  }

}
