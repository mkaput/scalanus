package edu.scalanus.stdlib

import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusStdLib {
  def addStdLib(scalanusScriptContext: ScalanusScriptContext): Unit ={
    scalanusScriptContext.setAttribute("IO", System.out, scalanusScriptContext.PROGRAM_SCOPE)
    scalanusScriptContext.setAttribute("Array", new ScalanusArray(), scalanusScriptContext.PROGRAM_SCOPE)
  }
}

class ScalanusArray{
  def of(args: Any*): Array[Any] = {
    Array(args:_*)
  }
}
