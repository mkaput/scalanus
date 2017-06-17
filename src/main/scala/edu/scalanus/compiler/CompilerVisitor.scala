package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusCompileException, ScalanusException}
import edu.scalanus.ir._
import edu.scalanus.parser.{ScalanusBaseVisitor, ScalanusParser}
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: ScalanusErrorListener)
  extends ScalanusBaseVisitor[Option[IrNode]] {

  override def visitAssignStmt(ctx: ScalanusParser.AssignStmtContext): Option[IrNode] = compileF(ctx) {
    for (
      pattern <- ctx.pattern().accept(this);
      expr <- ctx.expr().accept(this)
    )
      yield IrAssignStmt(pattern.asInstanceOf[IrPattern], expr.asInstanceOf[IrExpr])
  }

  override def visitBlock(ctx: ScalanusParser.BlockContext): Option[IrNode] = compile(ctx) {
    IrBlock(
      ctx.stmts.stmt.asScala
        .flatMap(_.accept(this))
        .map(_.asInstanceOf[IrStmt])
        .toIndexedSeq
    )
  }

  override def visitBlockExpr(ctx: ScalanusParser.BlockExprContext): Option[IrNode] =
    ctx.block().accept(this)

  override def visitExprStmt(ctx: ScalanusParser.ExprStmtContext): Option[IrNode] =
    ctx.expr.accept(this)

  override def visitIdxAccPattern(ctx: ScalanusParser.IdxAccPatternContext): Option[IrNode] = compileF(ctx) {
    for (
      recv <- ctx.expr(0).accept(this);
      idx <- ctx.expr(1).accept(this);
      idxAcc <- compile(ctx) {
        IrIdxAcc(recv.asInstanceOf[IrExpr], idx.asInstanceOf[IrExpr])
      }
    ) yield
      IrRefPattern(idxAcc.asInstanceOf[IrIdxAcc])
  }


  override def visitLiteral(ctx: ScalanusParser.LiteralContext): Option[IrNode] = compile(ctx) {
    IrValue(LiteralParser.parse(ctx))
  }

  override def visitLiteralExpr(ctx: ScalanusParser.LiteralExprContext): Option[IrNode] =
    ctx.literal.accept(this)

  override def visitMemAccPattern(ctx: ScalanusParser.MemAccPatternContext): Option[IrNode] = compileF(ctx) {
    for (
      recv <- ctx.expr().accept(this);
      memAcc <- compile(ctx) {
        IrMemAcc(recv.asInstanceOf[IrExpr], ctx.IDENT().getText)
      }
    ) yield
      IrRefPattern(memAcc.asInstanceOf[IrMemAcc])
  }

  override def visitPath(ctx: ScalanusParser.PathContext): Option[IrNode] = compile(ctx) {
    IrPath(ctx.IDENT.getText)
  }

  override def visitPathExpr(ctx: ScalanusParser.PathExprContext): Option[IrNode] = compileF(ctx) {
    for (path <- ctx.path().accept(this))
      yield IrRefExpr(path.asInstanceOf[IrRef])
  }

  override def visitPattern(ctx: ScalanusParser.PatternContext): Option[IrNode] = compile(ctx) {
    IrPattern(
      ctx.simplePattern.asScala
        .flatMap(_.accept(this))
        .map(_.asInstanceOf[IrSimplePattern])
        .toIndexedSeq
    )
  }

  override def visitWildcardPattern(ctx: ScalanusParser.WildcardPatternContext): Option[IrNode] = compile(ctx) {
    IrWildcardPattern()
  }

  override def visitPathPattern(ctx: ScalanusParser.PathPatternContext): Option[IrNode] = compileF(ctx) {
    for (path <- ctx.path().accept(this))
      yield IrRefPattern(path.asInstanceOf[IrRef])
  }

  override def visitValuePattern(ctx: ScalanusParser.ValuePatternContext): Option[IrNode] = compileF(ctx) {
    for (expr <- ctx.expr().accept(this))
      yield IrValuePattern(expr.asInstanceOf[IrExpr])
  }

  override def visitProgram(ctx: ScalanusParser.ProgramContext): Option[IrNode] = compile(ctx) {
    IrProgram(
      ctx.stmts.stmt.asScala
        .flatMap(_.accept(this))
        .map(_.asInstanceOf[IrStmt])
        .toIndexedSeq
    )
  }


  //
  // Utilities
  //

  /** Overriden to automatically throw not implemented ICE */
  override def visitChildren(node: RuleNode): Option[IrNode] = node.getRuleContext match {
    case ctx: ParserRuleContext => notImplemented(ctx)
    case _ => super.visitChildren(node)
  }

  private def ?!(message: String, ctx: ParserRuleContext): Option[IrNode] = {
    errors.report(new ScalanusCompileException(message, ctx))
    None
  }

  private def notImplemented(ctx: ParserRuleContext): Option[IrNode] =
    ?!(s"ICE: not implemented yet, ${ctx.getClass.getSimpleName.stripSuffix("Context")}", ctx)

  private def compile(ctx: ParserRuleContext)(f: => (IrCtx) => IrNode): Option[IrNode] = compileF(ctx) {
    Some(f)
  }


  private def compileF(ctx: ParserRuleContext)(f: => Option[(IrCtx) => IrNode]): Option[IrNode] =
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

}
