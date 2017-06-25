package edu.scalanus.interpreter

import javax.script.ScriptContext

import edu.scalanus.ir._

class ScalanusInterpreter {

  def eval(ir: IrNode, context: ScriptContext): AnyRef = {
    context match {
      case scalanusContext: ScalanusScriptContext => eval(ir, scalanusContext,scalanusContext.PROGRAM_SCOPE)
        .asInstanceOf[AnyRef]
      case _ => throw new IllegalArgumentException("Not Scalanus Context") // TODO: exception
    }
  }

  def eval(ir: IrNode, context: ScalanusScriptContext, scope:Int): Any = {
    ir match {
      case irProgram:IrProgram => evalProgram(irProgram,context,scope)
      case irBlock:IrBlock => evalBlock(irBlock,context,scope)
      case irRef:IrRef => evalRef(irRef,context,scope)
      case irStmt:IrStmt => evalStmt(irStmt,context,scope)
      case irPattern:IrPattern => evalPattern(irPattern,context,scope)
      case irExpr:IrExpr => evalExpr(irExpr,context,scope)
      case _ => ???
    }
  }

  def evalProgram(irProgram: IrProgram, context: ScalanusScriptContext, scope: Int): Any = {
    var result:Any = Unit
    for(stmt <- irProgram.stmts){
      result = evalStmt(stmt,context,scope)
    }
    result
  }

  def evalBlock(irBlock: IrBlock, context: ScalanusScriptContext, scope: Int): Any = {
    var result:Any = Unit
    for(stmt <- irBlock.stmts){
      result = evalStmt(stmt,context,scope)
    }
    result
  }

  //
  // References
  //

  def evalRef(irRef: IrRef, context: ScalanusScriptContext, scope: Int): Any = {
    irRef match{
      case irPath:IrPath => evalPath(irPath,context,scope)
      case irMemAcc:IrMemAcc => evalMemAcc(irMemAcc,context,scope)
      case irIdxAcc:IrIdxAcc => evalIdxAcc(irIdxAcc,context,scope)
      case _ => ???
    }
  }

  def evalPath(irPath: IrPath, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalMemAcc(irMemAcc: IrMemAcc, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalIdxAcc(irIdxAcc: IrIdxAcc, context: ScalanusScriptContext, scope: Int): Any = ???

  //
  // Statements
  //

  def evalAssignStmt(irAssignStmt: IrAssignStmt, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalStmt(irStmt: IrStmt, context: ScalanusScriptContext, scope: Int): Any = {
    irStmt match{
      case irAssignStmt:IrAssignStmt => evalAssignStmt(irAssignStmt,context,scope)
      case irItem:IrItem => evalItem(irItem,context,scope)
      case irExpr:IrExpr => evalExpr(irExpr,context,scope)
      case _ => ???
    }
  }

  //
  // Patterns
  //

  def evalPattern(irPattern: IrPattern, context: ScalanusScriptContext, scope: Int): Any = ???

  //
  // Items
  //

  def evalItem(irItem: IrItem, context: ScalanusScriptContext, scope: Int): Any = ???

  //
  // Expressions
  //


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

  def evalDictElem(irDictElem: IrExpr with IrDictElem, context: ScalanusScriptContext, scope: Int): Any = ???

  def evalExpr(irExpr: IrExpr, context: ScalanusScriptContext, scope: Int): Any = {
    irExpr match{
      case irBlock:IrBlock => evalBlock(irBlock,context,scope)
      case irRefExpr:IrRefExpr => evalRefExpr(irRefExpr,context,scope)
      case irValue:IrValue => evalValue(irValue,context,scope)
      case irUnaryExpr:IrUnaryExpr => evalUnaryExpr(irUnaryExpr,context,scope)
      case irIncrExpr:IrIncrExpr => evalIncrExpr(irIncrExpr,context,scope)
      case irBinaryExpr:IrBinaryExpr => evalBinaryExpr(irBinaryExpr,context,scope)
      case irFnCallExpr:IrFnCallExpr => evalFnCallExpr(irFnCallExpr,context,scope)
      case irForExpr:IrForExpr => evalForExpr(irForExpr,context,scope)
      case irWhileExpr:IrWhileExpr => evalWhileExpr(irWhileExpr,context,scope)
      case irLoopExpr:IrLoopExpr => evalLoopExpr(irLoopExpr,context,scope)
      case irIfExpr:IrIfExpr => evalIfExpr(irIfExpr,context,scope)
      case irBreak:IrBreak => evalBreak(irBreak,context,scope)
      case irContinue:IrContinue => eval(irContinue,context,scope)
      case irReturn:IrReturn => eval(irReturn,context,scope)
      case irTuple:IrTuple => evalTuple(irTuple,context,scope)
      case irDict:IrDict => evalDict(irDict,context,scope)
      case irDictElem:IrDictElem => evalDictElem(irDictElem,context,scope)
      case _ => ???
    }
  }

}
