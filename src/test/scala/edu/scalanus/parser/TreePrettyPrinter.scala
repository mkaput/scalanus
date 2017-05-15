package edu.scalanus.parser

import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree._
import org.antlr.v4.runtime.{Parser, ParserRuleContext}

class TreePrettyPrinter(private val parser: Parser) extends ParseTreeListener {
  private val INDENT = "    "
  private val builder = new StringBuilder
  private var indent: Int = -1

  override def visitTerminal(node: TerminalNode): Unit = {
    builder.append(INDENT * (indent + 1))
    builder.append(parser.getVocabulary.getDisplayName(node.getSymbol.getType))
    builder.append(": ")
    builder.append(Utils.escapeWhitespace(Trees.getNodeText(node, parser), false))
    builder.append('\n')
  }

  override def visitErrorNode(node: ErrorNode): Unit = {
    builder.append(INDENT * (indent + 1))
    builder.append("ERROR: ")
    builder.append(Utils.escapeWhitespace(Trees.getNodeText(node, parser), false))
    builder.append('\n')
  }

  override def enterEveryRule(ctx: ParserRuleContext): Unit = {
    indent += 1

    val ruleIndex = ctx.getRuleIndex
    val ruleName = parser.getRuleNames.lift(ruleIndex).getOrElse(ruleIndex.toString)
    var ctxName = ctx.getClass.getSimpleName.stripSuffix("Context")
    ctxName = ctxName(0).toLower + ctxName.substring(1)

    builder.append(INDENT * indent)
    builder.append(ruleName)
    if(!ctxName.isEmpty && ctxName != ruleName) {
      builder.append(":")
      builder.append(ctxName)
    }

    builder.append('\n')
  }

  override def exitEveryRule(ctx: ParserRuleContext): Unit = {
    indent -= 1
  }

  override def toString: String = builder.toString()
}

object TreePrettyPrinter {
  def apply(tree: ParseTree, parser: Parser): String = {
    val listener = new TreePrettyPrinter(parser)
    ParseTreeWalker.DEFAULT.walk(listener, tree)
    listener.toString
  }
}
