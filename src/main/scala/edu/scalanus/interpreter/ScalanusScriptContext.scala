package edu.scalanus.interpreter

import java.util.Objects
import javax.script.{ScriptContext, SimpleBindings, SimpleScriptContext}

import scala.collection.mutable.ArrayBuffer

class ScalanusScriptContext() extends SimpleScriptContext {

  val PROGRAM_SCOPE:Int = ScriptContext.GLOBAL_SCOPE+1
  private var scalanusScopes = ArrayBuffer.empty[ScalanusScope]
  addScope()

  override def setAttribute(name: String, value: scala.Any, scope: Int): Unit = {
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length) {
      super.setAttribute(name, value, scope)
      return
    }
    scalanusScopes(scopeId) match{
      case _:HardScope =>
        scalanusScopes(scopeId).put(name,value)
      case _:SoftScope =>
        val scope = scalanusScopes
          .slice(0,scopeId+1)
          .reverseIterator
          .collectFirst{
            case s if s.containsKey(name) => s
            case _:HardScope => scalanusScopes(scopeId)
          }
          .get // globalScope is HardScope
        scope.put(name,value)
    }
  }

  override def getAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      return super.getAttribute(name,scope)
    scalanusScopes(scopeId) match{
      case s if s.containsKey(name) || scopeId == 0 =>
        s.get(name)
      case _:HardScope =>
        getAttribute(name,PROGRAM_SCOPE)
      case _:SoftScope =>
        getAttribute(name,scope-1)
    }
  }

  override def removeAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      super.removeAttribute(name,scope)
    else
      scalanusScopes(scopeId).remove(name)
  }

  private def checkName(name: String) = {
    Objects.requireNonNull(name)
    if(name.isEmpty)
      throw new IllegalArgumentException("name cannot be empty")
  }

  def addScope(hardScope:Boolean=true):Int = {
    scalanusScopes += (if(hardScope) new HardScope() else new SoftScope())
    scalanusScopes.length + PROGRAM_SCOPE -1
  }

  def deleteScope(scope: Int): Unit = {
    val scopeId = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || scopeId >= scalanusScopes.length)
      throw new IllegalArgumentException("Illegal scope value.")
    scalanusScopes = scalanusScopes.dropRight(scalanusScopes.length-scopeId)
  }

}

sealed class ScalanusScope extends SimpleBindings

class SoftScope() extends ScalanusScope

class HardScope() extends ScalanusScope


