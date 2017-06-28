package edu.scalanus.interpreter

import edu.scalanus.interpreter.ScalanusInterpreter.{evalAssignStmt, evalExpr}
import edu.scalanus.ir.{IrAssignStmt, IrFnItem, IrValue}

sealed trait ScalanusFunction {
  def eval(args: IndexedSeq[Any], context: ScalanusScriptContext, scope: Int): Any
}

case class ScalanusMethod(recv:Any, methodName:String) extends ScalanusFunction{
  override def eval(args: IndexedSeq[Any], context: ScalanusScriptContext, scope: Int): Any =
    recv.getClass
      .getMethod(methodName,args.map(arg => arg.getClass):_*)
      .invoke(recv,args.map(arg => arg.asInstanceOf[AnyRef]):_*)
}

case class ScalanusIrFunction(irFnItem: IrFnItem) extends ScalanusFunction{
  override def eval(args: IndexedSeq[Any], context: ScalanusScriptContext, scope: Int): Any = {
    val newScope = context.addHardScope()
    if(irFnItem.params.nonEmpty)
      evalAssignStmt(IrAssignStmt(irFnItem.params.get, IrValue(args)(irFnItem.ctx))(irFnItem.ctx), context, newScope)
    try{
      evalExpr(irFnItem.routine, context, newScope)
    } catch {
      case scalanusReturn: ScalanusReturn => scalanusReturn.value
    }
    finally {
      context.deleteScope(newScope)
    }
  }
}
