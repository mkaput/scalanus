package edu.scalanus.interpreter

import javax.script.ScriptContext

import edu.scalanus.ir._

import scala.collection.GenIterable

object ScalanusInterpreter {

  def eval(ir: IrNode, context: ScriptContext): AnyRef = context match {
    case scalanusContext: ScalanusScriptContext =>
      eval(ir, scalanusContext, scalanusContext.PROGRAM_SCOPE).asInstanceOf[AnyRef]
  }

  def eval(ir: IrNode, context: ScalanusScriptContext, scope:Int): Any = ir match {
    case irProgram: IrProgram => evalProgram(irProgram, context, scope)
    case irRef: IrRef => evalRef(irRef, context, scope)
    case irStmt: IrStmt => evalStmt(irStmt, context, scope)
    case irPattern: IrPattern => evalPattern(irPattern, context, scope)
    case irSimplePattern: IrSimplePattern => evalSimplePattern(irSimplePattern, context, scope)
    case irDictElem: IrDictElem => evalDictElem(irDictElem,context,scope)
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
  }

  def setRef(irRef: IrRef, value: Any, context: ScalanusScriptContext, scope: Int): Any = irRef match {
    case irPath: IrPath => setPath(irPath,value, context, scope)
    case irMemAcc: IrMemAcc => setMemAcc(irMemAcc, value, context, scope)
    case irIdxAcc: IrIdxAcc => setIdxAcc(irIdxAcc, value, context, scope)
  }

  def evalPath(irPath: IrPath, context: ScalanusScriptContext, scope: Int): Any =
    context.getAttribute(irPath.ident, scope)

  def setPath(irPath: IrPath, value: Any, context: ScalanusScriptContext, scope: Int): Any =
    context.setAttribute(irPath.ident, value, scope)

  def evalMemAcc(irMemAcc: IrMemAcc, context: ScalanusScriptContext, scope: Int): Any = {
    val recv = evalExpr(irMemAcc.recv, context, scope)
    val cls = recv.getClass
    val method = cls.getMethods.filter(p => p.getName.equals(irMemAcc.member))
    if(method.nonEmpty){
      (args: Seq[AnyRef]) => method(0).invoke(recv, args :_*)
    }
    else cls.getField(irMemAcc.member).get(recv)
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
      case seq: Seq[Any] => seq(idx.asInstanceOf[Int])
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
    }
  }


  //
  // Statements
  //

  def evalStmt(irStmt: IrStmt, context: ScalanusScriptContext, scope: Int): Any = irStmt match {
    case irAssignStmt: IrAssignStmt => evalAssignStmt(irAssignStmt, context, scope)
    case irItem: IrItem => evalItem(irItem, context, scope)
    case irExpr: IrExpr => evalExpr(irExpr, context, scope)
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
            case _ => Array(value)
          }
        irAssignStmt.pattern.patterns.zip(values)
      }
    list.foreach((p:(IrSimplePattern,Any)) => {
      p._1 match {
        case _: IrWildcardPattern => // do nothing
        case irValuePattern: IrValuePattern =>
          if(evalExpr(irValuePattern.expr, context, scope) != p._2) throw new MatchError() // todo
        case irRefPattern: IrRefPattern =>
          setRef(irRefPattern.ref, p._2, context, scope)
      }
    })
  }



  //
  // Patterns
  //

  def evalPattern(irPattern: IrPattern, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalSimplePattern(irSimplePattern: IrSimplePattern, context: ScalanusScriptContext, scope: Int): Any = ???


  //
  // Items
  //

  def evalItem(irItem: IrItem, context: ScalanusScriptContext, scope: Int): Any = irItem match{
    case irFnItem: IrFnItem => evalFnItem(irFnItem, context, scope)
  }

  def evalFnItem(irFnItem: IrFnItem, context: ScalanusScriptContext, scope: Int): Any =
    context.setAttribute(irFnItem.name, irFnItem, scope)


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
      case IrBNotOp => value.asInstanceOf[Int].unary_~
      case IrNotOp => value.asInstanceOf[Boolean].unary_!
      case IrMinusOp =>
        value match{
          case int: Int => int.unary_-
          case double: Double => double.unary_-
        }
      case IrPlusOp =>
        value match{
          case int: Int => int.unary_+
          case double: Double => double.unary_+
        }
    }
    //value.getClass.getMethod("unary_" + irUnaryExpr.op.rep).invoke(value)
  }

  def evalIncrExpr(irIncrExpr: IrIncrExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val value = evalRefExpr(irIncrExpr.ref, context, scope)
    val op = irIncrExpr.op match{
      case IrPostfixDecrOp => IrSubOp
      case IrPostfixIncrOp => IrAddOp
      case IrPrefixDecrOp => IrSubOp
      case IrPrefixIncrOp => IrAddOp
    }
    val irBinaryExpr = IrBinaryExpr(op, IrValue(value)(irIncrExpr.ctx), IrValue(1)(irIncrExpr.ctx))(irIncrExpr.ctx)
    val newValue = evalBinaryExpr(irBinaryExpr, context, scope)
    setRef(irIncrExpr.ref.ref, newValue, context, scope)
    irIncrExpr.op match{
      case IrPostfixDecrOp => value
      case IrPostfixIncrOp => value
      case IrPrefixDecrOp => newValue
      case IrPrefixIncrOp => newValue
    }
  }

  def evalBinaryExpr(irBinaryExpr: IrBinaryExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val leftValue = evalExpr(irBinaryExpr.left, context, scope)
    val rightValue = evalExpr(irBinaryExpr.right, context, scope)
    leftValue.getClass.getMethod(irBinaryExpr.op.rep).invoke(leftValue, rightValue.asInstanceOf[AnyRef])
  }

  def evalFnCallExpr(irFnCallExpr: IrFnCallExpr, context: ScalanusScriptContext, scope: Int): Any = {
    val args = irFnCallExpr.args.map(arg => evalExpr(arg, context, scope))
    evalExpr(irFnCallExpr.fnExpr, context, scope) match {
      case irFnItem: IrFnItem =>
        val newScope = context.addHardScope()
        if(irFnItem.params.nonEmpty){
          evalAssignStmt(IrAssignStmt(irFnItem.params.get, IrValue(args)(irFnItem.ctx))(irFnItem.ctx),
            context,
            newScope)
        }
        var value: Any = Unit
        try{
          value = evalExpr(irFnItem.routine, context, newScope)
        } catch {
          case scalanusReturn: ScalanusReturn => value = scalanusReturn.value
        }
        context.deleteScope(newScope)
        value
      case any => any.asInstanceOf[(Seq[Any] => Any)](args)
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
      evalExpr(irIfExpr.ifBranch, context, scope)
  }

  def evalBreak(irBreak: IrBreak, context: ScalanusScriptContext, scope: Int): Any = throw ScalanusBreak()

  def evalContinue(irContinue: IrContinue, context: ScalanusScriptContext, scope: Int): Any = throw ScalanusContinue()

  def evalReturn(irReturn: IrReturn, context: ScalanusScriptContext, scope: Int): Any =
    throw ScalanusReturn(irReturn.value)

  def evalTuple(irTuple: IrTuple, context: ScalanusScriptContext, scope: Int): Any =
    irTuple.values.map(value => evalExpr(value, context, scope)).asInstanceOf[Array[Any]]

  def evalDict(irDict: IrDict, context: ScalanusScriptContext, scope: Int): Any = {
    val dict = collection.mutable.Map.empty[Any,Any]
    irDict.elements.foreach(
      elem => dict.put(evalExpr(elem.key, context, scope), evalExpr(elem.value, context, scope))
    )
    dict
  }

  def evalDictElem(irDictElem: IrDictElem, context: ScalanusScriptContext, scope: Int): Any = ???

}
