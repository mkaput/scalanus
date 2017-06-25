package edu.scalanus.interpreter

import javax.script.ScriptContext

import edu.scalanus.ir._

class ScalanusInterpreter {
  def eval(ir: IrNode, context: ScriptContext): AnyRef = {
    context match {
      case scalanusContext: ScalanusScriptContext => eval(ir, scalanusContext,scalanusContext.PROGRAM_SCOPE)
      case _ => throw new IllegalArgumentException("Not Scalanus Context") // TODO: exception
    }
  }

  def evalProgram(irProgram: IrProgram, context: ScalanusScriptContext, scope: Int): AnyRef = {
    var result:AnyRef = Unit
    for(stmt <- irProgram.stmts){
      result = evalStmt(stmt,context,scope)
    }
    result
  }

  def evalBlock(irBlock: IrBlock, context: ScalanusScriptContext, scope: Int): AnyRef = {
    var result:AnyRef = Unit
    for(stmt <- irBlock.stmts){
      result = evalStmt(stmt,context,scope)
    }
    result
  }

  def evalRef(irRef: IrRef, context: ScalanusScriptContext, scope: Int): AnyRef = {
    irRef match{
      case irPath:IrPath => ???
      case irMemAcc:IrMemAcc => ???
      case irIdxAcc:IrIdxAcc => ???
      case _ => ???
    }
  }

  def evalStmt(irStmt: IrStmt, context: ScalanusScriptContext, scope: Int): AnyRef = {
    irStmt match{
      case irAssignStmt:IrAssignStmt => ???
      case irItem:IrItem => ???
      case irExpr:IrExpr => evalExpr(irExpr,context,scope)
      case _ => ???
    }
  }

  def evalPattern(irPattern: IrPattern, context: ScalanusScriptContext, scope: Int): AnyRef = ???

  def evalExpr(irExpr: IrExpr, context: ScalanusScriptContext, scope: Int): AnyRef = {
    irExpr match{
      case irRefExpr:IrRefExpr => ???
      case irValue:IrValue => ???
      case irUnaryExpr:IrUnaryExpr => ???
      case irIncrExpr:IrIncrExpr => ???
      case irBinaryExpr:IrBinaryExpr => ???
      case irFnCallExpr:IrFnCallExpr => ???
      case irForExpr:IrForExpr => ???
      case irWhileExpr:IrWhileExpr => ???
      case irLoopExpr:IrLoopExpr => ???
      case irIfExpr:IrIfExpr => ???
      case irBreak:IrBreak => ???
      case irContinue:IrContinue => ???
      case irReturn:IrReturn => ???
      case irTuple:IrTuple => ???
      case irDict:IrDict => ???
      case irDictElem:IrDictElem => ???
      case _ => ???
    }
  }

  def eval(ir: IrNode, context: ScalanusScriptContext, scope:Int): AnyRef = {
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

}
