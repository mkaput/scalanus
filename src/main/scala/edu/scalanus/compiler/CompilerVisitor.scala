package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusCompileException, ScalanusException}
import edu.scalanus.ir._
import edu.scalanus.parser.ScalanusParser.ExprContext
import edu.scalanus.parser.{ScalanusBaseVisitor, ScalanusParser}
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: ScalanusErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

  //
  // Compile monad
  //

  private def compile[T <: IrNode](ctx: ParserRuleContext)(f: => (IrCtx) => T): Option[T] = compileM(ctx) {
    Some(f)
  }

  private def compileM[T <: IrNode](ctx: ParserRuleContext)(f: => Option[(IrCtx) => T]): Option[T] =
    try {
      val node = f.map(_.apply(IrCtx(ctx)))
      if (!errors.hasErrors) {
        Some(node.get)
      } else {
        None
      }
    } catch {
      case ex: ScalanusException =>
        if (ex.position == null) ex.position = LcfPosition(ctx)
        errors.report(ex)
        None
    }

  private def accept[T <: IrNode](ctx: ParserRuleContext): Option[T] =
    ctx.accept(this).map(_.asInstanceOf[T])


  //
  // General AST nodes
  //

  override def visitAssignStmt(ctx: ScalanusParser.AssignStmtContext): Option[IrNode] = compileM(ctx) {
    for {
      pattern <- accept[IrPattern](ctx.pattern)
      expr <- accept[IrExpr](ctx.expr)
    } yield IrAssignStmt(pattern, expr)
  }

  override def visitBlock(ctx: ScalanusParser.BlockContext): Option[IrNode] = compile(ctx) {
    IrBlock(
      ctx.stmts.stmt.asScala
        .flatMap(accept[IrStmt])
        .toIndexedSeq
    )
  }

  override def visitBlockExpr(ctx: ScalanusParser.BlockExprContext): Option[IrNode] = accept(ctx.block)

  override def visitExprStmt(ctx: ScalanusParser.ExprStmtContext): Option[IrNode] = accept(ctx.expr)


  override def visitIdxAccExpr(ctx: ScalanusParser.IdxAccExprContext): Option[IrRefExpr] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr(0))
      idx <- accept[IrExpr](ctx.expr(1))
      idxAcc <- compile(ctx) {
        IrIdxAcc(recv, idx)
      }
    } yield IrRefExpr(idxAcc)
  }

  override def visitIdxAccPattern(ctx: ScalanusParser.IdxAccPatternContext): Option[IrNode] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr(0))
      idx <- accept[IrExpr](ctx.expr(1))
      idxAcc <- compile(ctx) {
        IrIdxAcc(recv, idx)
      }
    } yield IrRefPattern(idxAcc)
  }

  override def visitLiteral(ctx: ScalanusParser.LiteralContext): Option[IrNode] = compile(ctx) {
    IrValue(LiteralParser.parse(ctx))
  }

  override def visitLiteralExpr(ctx: ScalanusParser.LiteralExprContext): Option[IrNode] = accept(ctx.literal)

  override def visitMemAccExpr(ctx: ScalanusParser.MemAccExprContext): Option[IrRefExpr] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr)
      memAcc <- compile(ctx) {
        IrMemAcc(recv, ctx.IDENT.getText)
      }
    } yield IrRefExpr(memAcc)
  }

  override def visitMemAccPattern(ctx: ScalanusParser.MemAccPatternContext): Option[IrNode] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr)
      memAcc <- compile(ctx) {
        IrMemAcc(recv, ctx.IDENT.getText)
      }
    } yield IrRefPattern(memAcc)
  }

  override def visitParenExpr(ctx: ScalanusParser.ParenExprContext): Option[IrNode] = accept(ctx.expr)

  override def visitPath(ctx: ScalanusParser.PathContext): Option[IrNode] = compile(ctx) {
    IrPath(ctx.IDENT.getText)
  }

  override def visitPathExpr(ctx: ScalanusParser.PathExprContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefExpr(path)
  }

  override def visitPathPattern(ctx: ScalanusParser.PathPatternContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefPattern(path)
  }

  override def visitPattern(ctx: ScalanusParser.PatternContext): Option[IrNode] = compile(ctx) {
    IrPattern(
      ctx.simplePattern.asScala
        .flatMap(accept[IrSimplePattern])
        .toIndexedSeq
    )
  }

  override def visitProgram(ctx: ScalanusParser.ProgramContext): Option[IrNode] = compile(ctx) {
    IrProgram(
      ctx.stmts.stmt.asScala
        .flatMap(accept[IrStmt])
        .toIndexedSeq
    )
  }

  override def visitWildcardPattern(ctx: ScalanusParser.WildcardPatternContext): Option[IrNode] = compile(ctx) {
    IrWildcardPattern()
  }

  override def visitValuePattern(ctx: ScalanusParser.ValuePatternContext): Option[IrNode] = compileM(ctx) {
    for (expr <- accept[IrExpr](ctx.expr)) yield IrValuePattern(expr)
  }


  //
  // Unary expressions
  //

  override def visitBnotExpr(ctx: ScalanusParser.BnotExprContext): Option[IrNode] = compileUnary(IrBNotOp, ctx)

  override def visitNotExpr(ctx: ScalanusParser.NotExprContext): Option[IrNode] = compileUnary(IrNotOp, ctx)

  override def visitUnaryMinusExpr(ctx: ScalanusParser.UnaryMinusExprContext): Option[IrNode] = compileUnary(IrMinusOp, ctx)

  override def visitUnaryPlusExpr(ctx: ScalanusParser.UnaryPlusExprContext): Option[IrNode] = compileUnary(IrPlusOp, ctx)

  private def compileUnary(op: IrUnaryOp, ctx: ScalanusParser.ExprContext): Option[IrUnaryExpr] = compileM(ctx) {
    val exprCtx = ctx.getRuleContext(classOf[ExprContext], 0)
    for (expr <- accept[IrExpr](exprCtx)) yield IrUnaryExpr(op, expr)
  }


  //
  // Increments/decrements
  //

  override def visitPostfixDecrExpr(ctx: ScalanusParser.PostfixDecrExprContext): Option[IrNode] = compileIncr(IrPostfixDecrOp, ctx)

  override def visitPostfixIncrExpr(ctx: ScalanusParser.PostfixIncrExprContext): Option[IrNode] = compileIncr(IrPostfixIncrOp, ctx)

  override def visitPrefixDecrExpr(ctx: ScalanusParser.PrefixDecrExprContext): Option[IrNode] = compileIncr(IrPrefixDecrOp, ctx)

  override def visitPrefixIncrExpr(ctx: ScalanusParser.PrefixIncrExprContext): Option[IrNode] = compileIncr(IrPrefixIncrOp, ctx)

  private def compileIncr(op: IrIncrOp, ctx: ScalanusParser.ExprContext): Option[IrIncrExpr] = compileM(ctx) {
    val exprCtx = ctx.getRuleContext(classOf[ExprContext], 0)
    for (expr <- accept[IrExpr](exprCtx))
      yield expr match {
        case refExpr: IrRefExpr => IrIncrExpr(op, refExpr)
        case _ => return ?!("expected reference expression", ctx)
      }
  }


  //
  // Binary expressions
  //

  override def visitAddExpr(ctx: ScalanusParser.AddExprContext): Option[IrNode] = compileBinary(IrAddOp, ctx)

  override def visitAndExpr(ctx: ScalanusParser.AndExprContext): Option[IrNode] = compileBinary(IrAndOp, ctx)

  override def visitBandExpr(ctx: ScalanusParser.BandExprContext): Option[IrNode] = compileBinary(IrBandOp, ctx)

  override def visitBitshiftLeftExpr(ctx: ScalanusParser.BitshiftLeftExprContext): Option[IrNode] = compileBinary(IrBitshiftLeftOp, ctx)

  override def visitBitshiftRightExpr(ctx: ScalanusParser.BitshiftRightExprContext): Option[IrNode] = compileBinary(IrBitshiftRightOp, ctx)

  override def visitBorExpr(ctx: ScalanusParser.BorExprContext): Option[IrNode] = compileBinary(IrBorOp, ctx)

  override def visitDivExpr(ctx: ScalanusParser.DivExprContext): Option[IrNode] = compileBinary(IrDivOp, ctx)

  override def visitEqExpr(ctx: ScalanusParser.EqExprContext): Option[IrNode] = compileBinary(IrEqOp, ctx)

  override def visitGteqExpr(ctx: ScalanusParser.GteqExprContext): Option[IrNode] = compileBinary(IrGteqOp, ctx)

  override def visitGtExpr(ctx: ScalanusParser.GtExprContext): Option[IrNode] = compileBinary(IrGtOp, ctx)

  override def visitLteqExpr(ctx: ScalanusParser.LteqExprContext): Option[IrNode] = compileBinary(IrLteqOp, ctx)

  override def visitLtExpr(ctx: ScalanusParser.LtExprContext): Option[IrNode] = compileBinary(IrLtOp, ctx)

  override def visitModExpr(ctx: ScalanusParser.ModExprContext): Option[IrNode] = compileBinary(IrModOp, ctx)

  override def visitMulExpr(ctx: ScalanusParser.MulExprContext): Option[IrNode] = compileBinary(IrMulOp, ctx)

  override def visitNeqExpr(ctx: ScalanusParser.NeqExprContext): Option[IrNode] = compileBinary(IrNeqOp, ctx)

  override def visitOrExpr(ctx: ScalanusParser.OrExprContext): Option[IrNode] = compileBinary(IrOrOp, ctx)

  override def visitPowExpr(ctx: ScalanusParser.PowExprContext): Option[IrNode] = compileBinary(IrPowOp, ctx)

  override def visitSubExpr(ctx: ScalanusParser.SubExprContext): Option[IrNode] = compileBinary(IrSubOp, ctx)

  override def visitXorExpr(ctx: ScalanusParser.XorExprContext): Option[IrNode] = compileBinary(IrXorOp, ctx)

  def compileBinary(op: IrBinaryOp, ctx: ExprContext): Option[IrBinaryExpr] = compileM(ctx) {
    val leftCtx = ctx.getRuleContext(classOf[ExprContext], 0)
    val rightCtx = ctx.getRuleContext(classOf[ExprContext], 1)
    for {
      left <- accept[IrExpr](leftCtx)
      right <- accept[IrExpr](rightCtx)
    } yield IrBinaryExpr(op, left, right)
  }


  //
  // Utilities
  //

  /** Overriden to automatically throw not implemented ICE */
  override def visitChildren(node: RuleNode): Option[IrNode] = node.getRuleContext match {
    case ctx: ParserRuleContext => ?!(s"ICE: not implemented yet, ${ctx.getClass.getSimpleName.stripSuffix("Context")}", ctx)
    case _ => super.visitChildren(node)
  }

  private def ?![T <: IrNode](message: String, ctx: ParserRuleContext): Option[T] = {
    errors.report(new ScalanusCompileException(message, ctx))
    None
  }

}
