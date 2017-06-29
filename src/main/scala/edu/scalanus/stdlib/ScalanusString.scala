package edu.scalanus.stdlib

import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusString extends ScalanusLib{

  def toInt(args: Any*): Any =
    if(args.size == 1){
      args(0).toString.toInt
    } else{
      args.map(arg => arg.toString.toInt)
    }

  override def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any =
    methodName match{
      case "toInt" => toInt(args:_*)
      case _ => ???
    }

}
