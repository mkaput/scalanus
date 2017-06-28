package edu.scalanus.stdlib

import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusStdLib {
  def addStdLib(scalanusScriptContext: ScalanusScriptContext): Unit ={
    scalanusScriptContext.setAttribute("IO", System.out, scalanusScriptContext.PROGRAM_SCOPE)
    scalanusScriptContext.setAttribute("Array", new ScalanusArray(), scalanusScriptContext.PROGRAM_SCOPE)
  }
}

trait ScalanusLib{
  def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any
}

class ScalanusArray extends ScalanusLib{
  def of(args: Any*): collection.mutable.IndexedSeq[Any] = {
    collection.mutable.ResizableArray(args:_*)
  }

  override def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any =
    methodName match{
      case "of" => of(args:_*)
    }

}
