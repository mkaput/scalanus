package edu.scalanus.compiler

import edu.scalanus.errors.ScalanusCompileException
import edu.scalanus.ir.{IrNode, IrProgram}
import edu.scalanus.parser.{ScalanusBaseVisitor, ScalanusParser}
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: CompilerErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

  override def visitProgram(ctx: ScalanusParser.ProgramContext): Option[IrNode] = compile {
    IrProgram(
      ctx.stmts().stmt().asScala
        .map(_.accept(this).orNull)
        .toIndexedSeq
    )
  } of ctx


  /*
   * Utilities
   */

  /** Overriden to automatically throw not implemented ICE */
  override def visitChildren(node: RuleNode): Option[IrNode] = node.getRuleContext match {
    case ctx: ParserRuleContext => ????(ctx)
    case _ => super.visitChildren(node)
  }

  private def raise(message: String, ctx: ParserRuleContext): Option[IrNode] = {
    errors.report(ScalanusCompileException(message, ctx))
    None
  }

  private def ????(ctx: ParserRuleContext): Option[IrNode] =
    raise("not implemented yet", ctx)

  private object compile {
    def apply(f: => LcfPosition => IrNode): compile = new compile(f)
  }

  private class compile(f: (LcfPosition) => IrNode) {
    def of(ctx: ParserRuleContext): Option[IrNode] =
      if (!errors.hasErrors) {
        Some(f(LcfPosition(ctx)))
      } else {
        None
      }
  }

}
