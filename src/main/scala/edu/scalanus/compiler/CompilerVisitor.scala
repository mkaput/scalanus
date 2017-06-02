package edu.scalanus.compiler

import edu.scalanus.errors.ScalanusCompileException
import edu.scalanus.ir.IrNode
import edu.scalanus.parser.ScalanusBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

class CompilerVisitor(private val errors: CompilerErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

  override def visitChildren(node: RuleNode): Option[IrNode] = node.getRuleContext match {
    case ctx: ParserRuleContext => ????(ctx)
    case _ => ???
  }


  private def raise(message: String, ctx: ParserRuleContext): Option[IrNode] = {
    errors.report(ScalanusCompileException(message, ctx))
    None
  }

  private def ????(ctx: ParserRuleContext): Option[IrNode] =
    raise("not implemented yet", ctx)

}
