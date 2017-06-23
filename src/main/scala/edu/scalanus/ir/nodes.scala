package edu.scalanus.ir

/** Root class in the IR class hierarchy */
sealed abstract class IrNode(val ctx: IrCtx) extends Product {

  def childrenNodes: Traversable[IrNode] =
    productIterator
      .flatMap {
        case collection: Traversable[_] =>
          collection.collect { case n: IrNode => n }
        case opt: Option[_] =>
          opt.collect { case n: IrNode => n }
        case node: IrNode => Array(node)
        case _ => Nil
      }
      .toTraversable

  override def toString: String = s"${getClass.getSimpleName}($ctx)"

}


case class IrProgram(stmts: IndexedSeq[IrStmt])(ctx: IrCtx) extends IrNode(ctx)

case class IrBlock(stmts: IndexedSeq[IrStmt])(ctx: IrCtx) extends IrNode(ctx) with IrExpr


//
// References
//

sealed trait IrRef extends IrNode

case class IrPath(ident: String)(ctx: IrCtx) extends IrNode(ctx) with IrRef {
  override def toString: String = s"${super.toString}: $ident"
}

case class IrMemAcc(recv: IrExpr, member: String)(ctx: IrCtx) extends IrNode(ctx) with IrRef {
  override def toString = s"${super.toString} .$member"
}

case class IrIdxAcc(recv: IrExpr, idx: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrRef


//
// Statements
//

sealed trait IrStmt extends IrNode

case class IrAssignStmt(pattern: IrPattern, expr: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrStmt


//
// Patterns
//

case class IrPattern(patterns: IndexedSeq[IrSimplePattern])(ctx: IrCtx) extends IrNode(ctx)

sealed trait IrSimplePattern extends IrNode

case class IrWildcardPattern()(ctx: IrCtx) extends IrNode(ctx) with IrSimplePattern

case class IrRefPattern(ref: IrRef)(ctx: IrCtx) extends IrNode(ctx) with IrSimplePattern

case class IrValuePattern(expr: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrSimplePattern


//
// Items
//

sealed trait IrItem extends IrStmt

case class IrFnItem(name: String, params: Option[IrPattern], routine: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrItem {
  override def toString = s"${super.toString}: $name"
}


//
// Expressions
//

sealed trait IrExpr extends IrNode with IrStmt

case class IrRefExpr(ref: IrRef)(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrValue(value: Any)(ctx: IrCtx) extends IrNode(ctx) with IrExpr {
  override def toString: String = s"${super.toString} = $value"
}

case class IrUnaryExpr(op: IrUnaryOp, expr: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr {
  override def toString = s"${super.toString}: $op"
}

case class IrIncrExpr(op: IrIncrOp, ref: IrRefExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr {
  override def toString = s"${super.toString}: $op"
}

case class IrBinaryExpr(op: IrBinaryOp, left: IrExpr, right: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr {
  override def toString = s"${super.toString}: $op"
}

case class IrFnCallExpr(fnExpr: IrExpr, args: IndexedSeq[IrExpr])(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrForExpr(pattern: IrPattern, producer: IrExpr, routine: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrWhileExpr(cond: IrExpr, routine: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrLoopExpr(routine: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrIfExpr(cond: IrExpr, ifBranch: IrExpr, elseBranch: Option[IrExpr])(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrBreak()(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrContinue()(ctx: IrCtx) extends IrNode(ctx) with IrExpr

case class IrReturn(value: IrExpr)(ctx: IrCtx) extends IrNode(ctx) with IrExpr
