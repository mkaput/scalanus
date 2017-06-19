package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusCompileException, ScalanusException}
import edu.scalanus.ir._
import edu.scalanus.parser.{ScalanusBaseVisitor, ScalanusParser}
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: ScalanusErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

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

  override def visitMemAccPattern(ctx: ScalanusParser.MemAccPatternContext): Option[IrNode] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr)
      memAcc <- compile(ctx) {
        IrMemAcc(recv, ctx.IDENT.getText)
      }
    } yield IrRefPattern(memAcc)
  }

  override def visitPath(ctx: ScalanusParser.PathContext): Option[IrNode] = compile(ctx) {
    IrPath(ctx.IDENT.getText)
  }

  override def visitPathExpr(ctx: ScalanusParser.PathExprContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefExpr(path)
  }

  override def visitPattern(ctx: ScalanusParser.PatternContext): Option[IrNode] = compile(ctx) {
    IrPattern(
      ctx.simplePattern.asScala
        .flatMap(accept[IrSimplePattern])
        .toIndexedSeq
    )
  }

  override def visitWildcardPattern(ctx: ScalanusParser.WildcardPatternContext): Option[IrNode] = compile(ctx) {
    IrWildcardPattern()
  }

  override def visitPathPattern(ctx: ScalanusParser.PathPatternContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefPattern(path)
  }

  override def visitValuePattern(ctx: ScalanusParser.ValuePatternContext): Option[IrNode] = compileM(ctx) {
    for (expr <- accept[IrExpr](ctx.expr)) yield IrValuePattern(expr)
  }

  override def visitProgram(ctx: ScalanusParser.ProgramContext): Option[IrNode] = compile(ctx) {
    IrProgram(
      ctx.stmts.stmt.asScala
        .flatMap(accept[IrStmt])
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

}
