package edu.scalanus.stdlib

import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusStdLib {
  def addStdLib(scalanusScriptContext: ScalanusScriptContext): Unit ={
    scalanusScriptContext.setAttribute("IO", ScalanusIO, scalanusScriptContext.PROGRAM_SCOPE)
    scalanusScriptContext.setAttribute("Array", ScalanusArray, scalanusScriptContext.PROGRAM_SCOPE)
    scalanusScriptContext.setAttribute("String", ScalanusString, scalanusScriptContext.PROGRAM_SCOPE)
  }
}

trait ScalanusLib{
  def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any
}
