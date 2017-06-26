package edu.scalanus.interpreter

import javax.script.ScriptContext

import edu.scalanus.ir._

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
    irProgram.stmts.foldLeft[Any] (Unit) { (_, stmt) => evalStmt(stmt, context, scope) }


  //
  // References
  //

  def evalRef(irRef: IrRef, context: ScalanusScriptContext, scope: Int): Any = irRef match {
    case irPath: IrPath => evalPath(irPath, context, scope)
    case irMemAcc: IrMemAcc => evalMemAcc(irMemAcc, context, scope)
    case irIdxAcc: IrIdxAcc => evalIdxAcc(irIdxAcc, context, scope)
  }

  def setRef(irRef: IrRef, context: ScalanusScriptContext, scope: Int, value: Any): Any = irRef match {
    case irPath: IrPath => setPath(irPath, context, scope, value)
    case irMemAcc: IrMemAcc => setMemAcc(irMemAcc, context, scope, value)
    case irIdxAcc: IrIdxAcc => setIdxAcc(irIdxAcc, context, scope, value)
  }

  def evalPath(irPath: IrPath, context: ScalanusScriptContext, scope: Int): Any =
    context.getAttribute(irPath.ident, scope)

  def setPath(irPath: IrPath, context: ScalanusScriptContext, scope: Int, value: Any): Any =
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

  def setMemAcc(irMemAcc: IrMemAcc, context: ScalanusScriptContext, scope: Int, value: Any): Any = {
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

  def setIdxAcc(irIdxAcc: IrIdxAcc, context: ScalanusScriptContext, scope: Int, value: Any): Any = {
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

  def evalAssignStmt(irAssignStmt: IrAssignStmt, context: ScalanusScriptContext, scope: Int): Any = ???


  //
  // Patterns
  //

  def evalPattern(irPattern: IrPattern, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalSimplePattern(irSimplePattern: IrSimplePattern, context: ScalanusScriptContext, scope: Int): Any =
    irSimplePattern match {
      case irWildcardPattern: IrWildcardPattern => evalWildcardPattern(irWildcardPattern, context, scope)
      case irRefPattern: IrRefPattern => evalRefPattern(irRefPattern, context, scope)
      case irValuePattern: IrValuePattern => evalValuePattern(irValuePattern, context, scope)
    }

  def evalWildcardPattern(irWildcardPattern: IrWildcardPattern, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalRefPattern(irRefPattern: IrRefPattern, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalValuePattern(irValuePattern: IrValuePattern, context: ScalanusScriptContext, scope: Int): Any = ???


  //
  // Items
  //

  def evalItem(irItem: IrItem, context: ScalanusScriptContext, scope: Int): Any = irItem match{
    case irFnItem: IrFnItem => evalFnItem(irFnItem, context, scope)
  }

  def evalFnItem(irFnItem: IrFnItem, context: ScalanusScriptContext, scope: Int): Any = ???


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
      case irContinue: IrContinue => eval(irContinue, context, scope)
      case irReturn: IrReturn => eval(irReturn, context, scope)
      case irTuple: IrTuple => evalTuple(irTuple, context, scope)
      case irDict: IrDict => evalDict(irDict, context, scope)
    }
  }

  def evalBlock(irBlock: IrBlock, context: ScalanusScriptContext, scope: Int): Any =
    irBlock.stmts.foldLeft[Any] (Unit) { (_, stmt) => evalStmt(stmt, context, scope) }

  def evalRefExpr(irRefExpr: IrRefExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalValue(irValue: IrValue, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalUnaryExpr(irUnaryExpr: IrUnaryExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalIncrExpr(irIncrExpr: IrIncrExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalBinaryExpr(irBinaryExpr: IrBinaryExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalFnCallExpr(irFnCallExpr: IrFnCallExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalForExpr(irForExpr: IrForExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalWhileExpr(irWhileExpr: IrWhileExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalLoopExpr(irLoopExpr: IrLoopExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalIfExpr(irIfExpr: IrIfExpr, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalBreak(irBreak: IrBreak, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalTuple(irTuple: IrTuple, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalDict(irDict: IrDict, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalDictElem(irDictElem: IrDictElem with IrDictElem, context: ScalanusScriptContext, scope: Int): Any = ???

}
