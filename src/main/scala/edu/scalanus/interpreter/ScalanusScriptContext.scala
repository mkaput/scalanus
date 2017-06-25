package edu.scalanus.interpreter

import java.util
import java.util.{ArrayList, Collections, Objects}
import javax.script.{Bindings, ScriptContext, SimpleBindings, SimpleScriptContext}

import scala.collection.mutable.ArrayBuffer

class ScalanusScriptContext() extends SimpleScriptContext {

  val PROGRAM_SCOPE:Int = ScriptContext.GLOBAL_SCOPE+1
  private val scopesArray = ArrayBuffer.empty[Bindings]
  private val scopesList = new util.ArrayList[Int]

  scopesList.add(ScriptContext.ENGINE_SCOPE)
  scopesList.add(ScriptContext.GLOBAL_SCOPE)
  addScope()


  override def getBindings(scope: Int): Bindings = {
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) super.getBindings(scope)
    else scopesArray(i)
  }

  override def setBindings(bindings: Bindings, scope: Int): Unit = {
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) super.setBindings(bindings,scope)
    else scopesArray(i) = bindings
  }

  override def getScopes: util.List[Integer] = scopesList.asInstanceOf[util.List[Integer]]

  override def setAttribute(name: String, value: scala.Any, scope: Int): Unit = {
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) super.setAttribute(name,value,scope)
    else scopesArray(i).put(name,value)
  }

  override def getAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) super.getAttribute(name,scope)
    else scopesArray(i).get(name)
  }

  override def removeAttribute(name: String, scope: Int): AnyRef = {
    checkName(name)
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) super.removeAttribute(name,scope)
    else scopesArray(i).remove(name)
  }

  private def checkName(name: String) = {
    Objects.requireNonNull(name)
    if (name.isEmpty) throw new IllegalArgumentException("name cannot be empty")
  }

  def addScope():Int = {
    val i = scopesArray.length + PROGRAM_SCOPE
    scopesArray += new SimpleBindings
    scopesList.add(i)
    i
  }

  def deleteScope(scope: Int): Unit = {
    val i = scope - PROGRAM_SCOPE
    if(scope < PROGRAM_SCOPE || i >= scopesArray.length) new IllegalArgumentException("Illegal scope value.")
    scopesList.removeIf((p) => p >= scope)
    scopesArray.dropRight(scopesArray.length-i)
  }

}


