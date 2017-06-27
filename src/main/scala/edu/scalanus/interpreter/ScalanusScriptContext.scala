package edu.scalanus.interpreter

import java.util.Objects
import javax.script.{ScriptContext, SimpleScriptContext}

import scala.collection.mutable.ArrayBuffer

class ScalanusScriptContext(context: ScriptContext) extends SimpleScriptContext {

  setBindings(context.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE)
  setBindings(context.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE)
  setWriter(context.getWriter)
  setReader(context.getReader)
  setErrorWriter(context.getErrorWriter)

  val PROGRAM_SCOPE: Int = ScriptContext.GLOBAL_SCOPE+1
  private var scalanusScopes = ArrayBuffer.empty[ScalanusScope]
  addHardScope()

  override def setAttribute(name: String, value: scala.Any, scope: Int): Unit = {
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length) {
      super.setAttribute(name, value, scope)
      return
    }
    scalanusScopes(scopeId) match{
      case _: HardScope =>
        scalanusScopes(scopeId).put(name, value)
      case _: SoftScope =>
        val scope = scalanusScopes
          .slice(0, scopeId+1)
          .reverseIterator
          .collectFirst{
            case s if s.containsKey(name) => s
            case _:HardScope => scalanusScopes(scopeId)
          }
          .get // globalScope is HardScope
        scope.put(name, value)
    }
  }

  override def getAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      return super.getAttribute(name, scope)
    scalanusScopes(scopeId) match{
      case s if s.containsKey(name) || scopeId == 0 => s.get(name)
      case _: HardScope => getAttribute(name, PROGRAM_SCOPE)
      case _: SoftScope => getAttribute(name, scope-1)
    }
  }

  override def removeAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      super.removeAttribute(name, scope)
    else
      scalanusScopes(scopeId).remove(name)
  }

  private def checkName(name: String) = {
    Objects.requireNonNull(name)
    if(name.isEmpty)
      throw new IllegalArgumentException("name cannot be empty")
  }

  private def addScope(scope: ScalanusScope):Int = {
    scalanusScopes += scope
    scalanusScopes.length + PROGRAM_SCOPE -1
  }

  def addSoftScope(): Int = addScope(SoftScope())

  def addHardScope(): Int = addScope(HardScope())

  def deleteScope(scope: Int): Unit = {
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      throw new IllegalArgumentException("Illegal scope value.")
    scalanusScopes = scalanusScopes.dropRight(scalanusScopes.length-scopeId)
  }

}

object ScalanusScriptContext{
  def apply() = new ScalanusScriptContext(new SimpleScriptContext)
}
