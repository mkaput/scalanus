package edu.scalanus.stdlib
import edu.scalanus.interpreter.ScalanusScriptContext

object ScalanusIO extends ScalanusLib{

  def print(args: Any*): Unit = {
    args.foreach(System.out.print)
  }

  def println(args: Any*): Unit = {
    args.foreach(System.out.println)
  }

  override def eval(methodName: String, args: Seq[Any], context: ScalanusScriptContext, scope: Int): Any =
    methodName match{
      case "print" => print(args:_*)
      case "println" => println(args:_*)
      case _ => ???
    }
}
