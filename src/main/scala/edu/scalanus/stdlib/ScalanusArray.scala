package edu.scalanus.stdlib

import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusArray extends ScalanusLib{
  def of(args: Any*): collection.mutable.IndexedSeq[Any] = {
    collection.mutable.ResizableArray(args:_*)
  }

  override def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any =
    methodName match{
      case "of" => of(args:_*)
      case _ => ???
    }

}
