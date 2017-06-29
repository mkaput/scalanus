package edu.scalanus.interpreter

import javax.script.ScriptContext

import edu.scalanus.errors.ScalanusEvalException
import edu.scalanus.ir._

import scala.collection.GenIterable

object ScalanusInterpreter {

  def eval(ir: IrNode, context: ScriptContext): AnyRef = context match {
    case scalanusContext: ScalanusScriptContext =>
      eval(ir, scalanusContext, scalanusContext.PROGRAM_SCOPE).asInstanceOf[AnyRef]
    case _ =>
      throw new ScalanusEvalException("Not Scalanus Context",ir.ctx)
  }

  def eval(ir: IrNode, context: ScalanusScriptContext, scope:Int): Any = ir match {
    case irProgram: IrProgram => evalProgram(irProgram, context, scope)
    case irRef: IrRef => evalRef(irRef, context, scope)
    case irStmt: IrStmt => evalStmt(irStmt, context, scope)
    case _ => throw new ScalanusEvalException("Unreachable Code",ir.ctx)
  }

  def evalProgram(irProgram: IrProgram, context: ScalanusScriptContext, scope: Int): Any =
    irProgram.stmts.foldLeft[Any](Unit) { (_, stmt) => evalStmt(stmt, context, scope) }


  //
  // References
  //

  def evalRef(irRef: IrRef, context: ScalanusScriptContext, scope: Int): Any = irRef match {
    case irPath: IrPath => evalPath(irPath, context, scope)
    case irMemAcc: IrMemAcc => evalMemAcc(irMemAcc, context, scope)
    case irIdxAcc: IrIdxAcc => evalIdxAcc(irIdxAcc, context, scope)
    case _ => throw new ScalanusEvalException("Unreachable Code",irRef.ctx)
  }

  def setRef(irRef: IrRef, value: Any, context: ScalanusScriptContext, scope: Int): Any = irRef match {
    case irPath: IrPath => setPath(irPath,value, context, scope)
    case irMemAcc: IrMemAcc => setMemAcc(irMemAcc, value, context, scope)
    case irIdxAcc: IrIdxAcc => setIdxAcc(irIdxAcc, value, context, scope)
    case _ => throw new ScalanusEvalException("Unreachable Code",irRef.ctx)
  }

  def evalPath(irPath: IrPath, context: ScalanusScriptContext, scope: Int): Any =
    context.getAttribute(irPath.ident, scope)

  def setPath(irPath: IrPath, value: Any, context: ScalanusScriptContext, scope: Int): Any =
    context.setAttribute(irPath.ident, value, scope)

  def evalMemAcc(irMemAcc: IrMemAcc, context: ScalanusScriptContext, scope: Int): Any = {
    val recv = evalExpr(irMemAcc.recv, context, scope)
    val cls = recv.getClass
    try{
      cls.getField(irMemAcc.member).get(recv)
    }
    catch{
      case _: java.lang.NoSuchFieldException =>
        ScalanusMethod(recv,irMemAcc.member)
    }
  }

  def setMemAcc(irMemAcc: IrMemAcc, value: Any, context: ScalanusScriptContext, scope: Int): Any = {
    val recv = evalExpr(irMemAcc.recv, context, scope)
    val cls = recv.getClass
    cls.getField(irMemAcc.member).set(recv,value)
  }

  def evalIdxAcc(irIdxAcc: IrIdxAcc, context: ScalanusScriptContext, scope: Int): Any = {
    val recv = evalExpr(irIdxAcc.recv, context, scope)
    val idx = evalExpr(irIdxAcc.idx, context, scope)
    recv match {
      case map: collection.mutable.Map[_,_] =>
        map.asInstanceOf[collection.mutable.Map[Any,Any]](idx)
      case seq: IndexedSeq[Any] => seq(idx.asInstanceOf[Int])
      case _ => throw new ScalanusEvalException("Get Value Error",irIdxAcc.ctx)
    }
  }

  def setIdxAcc(irIdxAcc: IrIdxAcc, value: Any, context: ScalanusScriptContext, scope: Int): Any = {
    val recv = evalExpr(irIdxAcc.recv, context, scope)
    val idx = evalExpr(irIdxAcc.idx, context, scope)
    recv match {
      case map: collection.mutable.Map[_,_] =>
        map.asInstanceOf[collection.mutable.Map[Any,Any]] += (idx -> value)
      case seq: collection.mutable.Seq[_] =>
        seq.asInstanceOf[collection.mutable.Seq[Any]](idx.asInstanceOf[Int]) = value
      case _ =>
        throw new ScalanusEvalException("Set Value Error",irIdxAcc.ctx)
    }
  }


  //
  // Statements
  //

  def evalStmt(irStmt: IrStmt, context: ScalanusScriptContext, scope: Int): Any = irStmt match {
    case irAssignStmt: IrAssignStmt => evalAssignStmt(irAssignStmt, context, scope)
    case irItem: IrItem => evalItem(irItem, context, scope)
    case irExpr: IrExpr => evalExpr(irExpr, context, scope)
    case _ => throw new ScalanusEvalException("Unreachable Code",irStmt.ctx)
  }

  def evalAssignStmt(irAssignStmt: IrAssignStmt, context: ScalanusScriptContext, scope: Int): Any ={
    val value = evalExpr(irAssignStmt.expr, context, scope)
    val list: Iterable[(IrSimplePattern,Any)] =
      if(irAssignStmt.pattern.patterns.size == 1){
        Array((irAssignStmt.pattern.patterns(0),value))
      } else{
        val values: GenIterable[Any] =
          value match {
            case iterable: GenIterable[Any] => iterable
            case product: Product => product.productIterator.toList
            case _ => Array(value)
          }
        irAssignStmt.pattern.patterns.zip(values)
      }
    list.foreach((p:(IrSimplePattern,Any)) => {
      p._1 match {
        case _: IrWildcardPattern =>
          // do nothing
        case irValuePattern: IrValuePattern =>
          if(evalExpr(irValuePattern.expr, context, scope) != p._2)
            throw new ScalanusEvalException("Pattern Match Error",irAssignStmt.ctx)
        case irRefPattern: IrRefPattern =>
          setRef(irRefPattern.ref, p._2, context, scope)
        case _ =>
          throw new ScalanusEvalException("Unreachable Code",irAssignStmt.ctx)
      }
    })
  }

  //
  // Items
  //

  def evalItem(irItem: IrItem, context: ScalanusScriptContext, scope: Int): Any = irItem match{
    case irFnItem: IrFnItem => evalFnItem(irFnItem, context, scope)
    case _ => throw new ScalanusEvalException("Unreachable Code",irItem.ctx)
  }

  def evalFnItem(irFnItem: IrFnItem, context: ScalanusScriptContext, scope: Int): Any =
    context.setAttribute(irFnItem.name, ScalanusIrFunction(irFnItem), scope)


  //
  // Expressions
  //

  def evalExpr(irExpr: IrExpr, context: ScalanusScriptContext, scope: Int): Any = {
    irExpr match{
      case irBlock: IrBlock => evalBlock(irBlock, context, scope)
      case irRefExpr: IrRefExpr => evalRefExpr(irRefExpr, context, scope)
      case irValue: IrValue => evalValue(irValue, context, scope)
      case irUnaryExpr: IrUnaryExpr => evalUnaryExpr(irUnaryExpr, context, scope)
      case irIncrExpr: IrIncrExpr => evalIncrExpr(irIncrExpr, context, scope)
      case irBinaryExpr: IrBinaryExpr => evalBinaryExpr(irBinaryExpr, context, scope)
      case irFnCallExpr: IrFnCallExpr => evalFnCallExpr(irFnCallExpr, context, scope)
      case irForExpr: IrForExpr => evalForExpr(irForExpr, context, scope)
      case irWhileExpr: IrWhileExpr => evalWhileExpr(irWhileExpr, context, scope)
      case irLoopExpr: IrLoopExpr => evalLoopExpr(irLoopExpr, context, scope)
      case irIfExpr: IrIfExpr => evalIfExpr(irIfExpr, context, scope)
      case irBreak: IrBreak => evalBreak(irBreak, context, scope)
      case irContinue: IrContinue => evalContinue(irContinue, context, scope)
      case irReturn: IrReturn => evalReturn(irReturn, context, scope)
      case irTuple: IrTuple => evalTuple(irTuple, context, scope)
      case irDict: IrDict => evalDict(irDict, context, scope)
      case _ => throw new ScalanusEvalException("Unreachable Code",irExpr.ctx)
    }
  }

  def evalBlock(irBlock: IrBlock, context: ScalanusScriptContext, scope: Int): Any ={
    val newScope = context.addSoftScope()
    val value = irBlock.stmts.foldLeft[Any] (Unit) { (_, stmt) => evalStmt(stmt, context, newScope) }
    context.deleteScope(newScope)
    value
  }


  def evalRefExpr(irRefExpr: IrRefExpr, context: ScalanusScriptContext, scope: Int): Any =
    evalRef(irRefExpr.ref, context, scope)

  def evalValue(irValue: IrValue, context: ScalanusScriptContext, scope: Int): Any = irValue.value

  def evalUnaryExpr(irUnaryExpr: IrUnaryExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val value = evalExpr(irUnaryExpr.expr, context, scope)
    irUnaryExpr.op match{
      case IrBNotOp =>
        value match{
          case int: Int => int.unary_~
          case _ => throw new ScalanusEvalException("Operator not found",irUnaryExpr.ctx)
        }
      case IrNotOp =>
        value match{
          case bool: Boolean => bool.unary_!
          case _ => throw new ScalanusEvalException("Operator not found",irUnaryExpr.ctx)
        }
      case IrMinusOp =>
        value match{
          case int: Int => int.unary_-
          case double: Double => double.unary_-
          case _ => throw new ScalanusEvalException("Operator not found",irUnaryExpr.ctx)
        }
      case IrPlusOp =>
        value match{
          case int: Int => int.unary_+
          case double: Double => double.unary_+
          case _ => throw new ScalanusEvalException("Operator not found",irUnaryExpr.ctx)
        }
      case _ => throw new ScalanusEvalException("Unreachable Code",irUnaryExpr.ctx)
    }
  }

  def evalIncrExpr(irIncrExpr: IrIncrExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val value = evalRefExpr(irIncrExpr.ref, context, scope)
    val op = irIncrExpr.op match{
      case IrPostfixDecrOp => IrSubOp
      case IrPostfixIncrOp => IrAddOp
      case IrPrefixDecrOp => IrSubOp
      case IrPrefixIncrOp => IrAddOp
      case _ => throw new ScalanusEvalException("Unreachable Code",irIncrExpr.ctx)
    }
    val irBinaryExpr = IrBinaryExpr(op, IrValue(value)(irIncrExpr.ctx), IrValue(1)(irIncrExpr.ctx))(irIncrExpr.ctx)
    val newValue = evalBinaryExpr(irBinaryExpr, context, scope)
    setRef(irIncrExpr.ref.ref, newValue, context, scope)
    irIncrExpr.op match{
      case IrPostfixDecrOp => value
      case IrPostfixIncrOp => value
      case IrPrefixDecrOp => newValue
      case IrPrefixIncrOp => newValue
      case _ => throw new ScalanusEvalException("Unreachable Code",irIncrExpr.ctx)
    }
  }

  def evalBinaryExpr(irBinaryExpr: IrBinaryExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val leftValue = evalExpr(irBinaryExpr.left, context, scope)
    val rightValue = evalExpr(irBinaryExpr.right, context, scope)
    irBinaryExpr.op match {
      case IrAddOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left + right
          case (left: Int,right: Double) => left + right
          case (left: Double,right: Int) => left + right
          case (left: Double,right: Double) => left + right
          case (left: String,right: Any) => left + right
          case (left: Any,right: String) => left + right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrAndOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left && right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrBandOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left & right
          case (left: Int,right: Int) => left & right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrBitshiftLeftOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left << right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrBitshiftRightOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left >> right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrBorOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left | right
          case (left: Int,right: Int) => left | right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrDivOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left / right
          case (left: Int,right: Double) => left / right
          case (left: Double,right: Int) => left / right
          case (left: Double,right: Double) => left / right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrEqOp =>
        leftValue == rightValue
      case IrGteqOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left >= right
          case (left: Int,right: Int) => left >= right
          case (left: Int,right: Double) => left >= right
          case (left: Double,right: Int) => left >= right
          case (left: Double,right: Double) => left >= right
          case (left: String,right: String) => left >= right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrGtOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left > right
          case (left: Int,right: Int) => left > right
          case (left: Int,right: Double) => left > right
          case (left: Double,right: Int) => left > right
          case (left: Double,right: Double) => left > right
          case (left: String,right: String) => left > right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrLteqOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left <= right
          case (left: Int,right: Int) => left <= right
          case (left: Int,right: Double) => left <= right
          case (left: Double,right: Int) => left <= right
          case (left: Double,right: Double) => left <= right
          case (left: String,right: String) => left <= right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrLtOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left < right
          case (left: Int,right: Int) => left < right
          case (left: Int,right: Double) => left < right
          case (left: Double,right: Int) => left < right
          case (left: Double,right: Double) => left < right
          case (left: String,right: String) => left < right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrModOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left % right
          case (left: Int,right: Double) => left % right
          case (left: Double,right: Int) => left % right
          case (left: Double,right: Double) => left % right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrMulOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left * right
          case (left: Int,right: Double) => left * right
          case (left: Double,right: Int) => left * right
          case (left: Double,right: Double) => left * right
          case (left: String,right: Int) => left * right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrNeqOp =>
        leftValue != rightValue
      case IrOrOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left || right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrPowOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => scala.math.pow(left, right)
          case (left: Int,right: Double) => scala.math.pow(left, right)
          case (left: Double,right: Int) => scala.math.pow(left, right)
          case (left: Double,right: Double) => scala.math.pow(left, right)
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrSubOp =>
        (leftValue,rightValue) match{
          case (left: Int,right: Int) => left - right
          case (left: Int,right: Double) => left - right
          case (left: Double,right: Int) => left - right
          case (left: Double,right: Double) => left - right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
      case IrXorOp =>
        (leftValue,rightValue) match{
          case (left: Boolean,right: Boolean) => left ^ right
          case (left: Int,right: Int) => left ^ right
          case _ => throw new ScalanusEvalException("Operator not found",irBinaryExpr.ctx)
        }
    }
  }

  def evalFnCallExpr(irFnCallExpr: IrFnCallExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val args = irFnCallExpr.args.map(arg => evalExpr(arg, context, scope))
    evalExpr(irFnCallExpr.fnExpr, context, scope) match {
      case scalanusFunction: ScalanusFunction =>
        scalanusFunction.eval(args, context, scope)
      case _ => throw new ScalanusEvalException("Function call Error",irFnCallExpr.ctx)
    }
  }

  def evalForExpr(irForExpr: IrForExpr, context: ScalanusScriptContext, scope: Int): Unit = {
    val newScope = context.addSoftScope()
    val it = evalExpr(irForExpr.producer, context, newScope).asInstanceOf[Iterable[Any]].iterator
    while(it.hasNext){
      evalAssignStmt(IrAssignStmt(irForExpr.pattern, IrValue(it.next())(irForExpr.ctx))(irForExpr.ctx),
        context,
        newScope)
      try {
        evalExpr(irForExpr.routine, context, newScope)
      } catch {
        case _: ScalanusBreak => return
        case _: ScalanusContinue => // do nothing
      }
    }
    context.deleteScope(newScope)
  }

  def evalWhileExpr(irWhileExpr: IrWhileExpr, context: ScalanusScriptContext, scope: Int): Unit = {
    while(evalExpr(irWhileExpr.cond, context, scope).equals(true)){
      try {
        evalExpr(irWhileExpr.routine, context, scope)
      } catch {
        case _: ScalanusBreak => return
        case _: ScalanusContinue => // do nothing
      }
    }
  }

  def evalLoopExpr(irLoopExpr: IrLoopExpr, context: ScalanusScriptContext, scope: Int): Unit = {
    while(true){
      try {
        evalExpr(irLoopExpr.routine, context, scope)
      } catch {
        case _: ScalanusBreak => return
        case _: ScalanusContinue => // do nothing
      }
    }

  }

  def evalIfExpr(irIfExpr: IrIfExpr, context: ScalanusScriptContext, scope: Int): Any = {
    if(eval(irIfExpr.cond, context, scope).equals(true))
      evalExpr(irIfExpr.ifBranch, context, scope)
    else if(irIfExpr.elseBranch.nonEmpty)
      evalExpr(irIfExpr.elseBranch.get, context, scope)
  }

  def evalBreak(irBreak: IrBreak, context: ScalanusScriptContext, scope: Int): Any = throw ScalanusBreak()

  def evalContinue(irContinue: IrContinue, context: ScalanusScriptContext, scope: Int): Any = throw ScalanusContinue()

  def evalReturn(irReturn: IrReturn, context: ScalanusScriptContext, scope: Int): Any =
    throw ScalanusReturn(evalExpr(irReturn.value, context, scope))

  def evalTuple(irTuple: IrTuple, context: ScalanusScriptContext, scope: Int): Any =
    irTuple.values.map(value => evalExpr(value, context, scope))

  def evalDict(irDict: IrDict, context: ScalanusScriptContext, scope: Int): Any = {
    val dict = collection.mutable.Map.empty[Any,Any]
    irDict.elements.foreach(
      elem => dict.put(evalExpr(elem.key, context, scope), evalExpr(elem.value, context, scope))
    )
    dict
  }

}
