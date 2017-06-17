package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusCompileException, ScalanusException}
import edu.scalanus.ir.{IrCtx, IrNode, IrProgram, IrValue}
import edu.scalanus.parser.{ScalanusBaseVisitor, ScalanusParser}
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: ScalanusErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

  override def visitExprStmt(ctx: ScalanusParser.ExprStmtContext): Option[IrNode] =
    ctx.expr().accept(this)

  override def visitLiteral(ctx: ScalanusParser.LiteralContext): Option[IrNode] = compile(ctx) {
    IrValue(LiteralParser.parse(ctx))
  }

  override def visitLiteralExpr(ctx: ScalanusParser.LiteralExprContext): Option[IrNode] =
    ctx.literal().accept(this)

  override def visitProgram(ctx: ScalanusParser.ProgramContext): Option[IrNode] = compile(ctx) {
    IrProgram(
      ctx.stmts().stmt().asScala
        .map(_.accept(this).orNull)
        .toIndexedSeq
    )
  }


  /*
   * Utilities
   */

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

  private def compile(ctx: ParserRuleContext)(f: => (IrCtx) => IrNode): Option[IrNode] =
    try {
      val node = f(IrCtx(LcfPosition(ctx)))
      if (!errors.hasErrors) {
        Some(node)
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
