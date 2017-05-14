package edu.scalanus.parser

import java.util

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.{Tree, Trees}

object TreePrettyPrinter {

  def prettyPrint(tree: Tree, parser: Option[Parser]): String = {
    val ruleNamesList = parser
      .flatMap(p => Option(p.getRuleNames))
      .map(a => util.Arrays.asList(a: _*))
      .orNull
    prettyPrint(tree, ruleNamesList)
  }

  def prettyPrint(tree: Tree, ruleNamesList: util.List[String] = null): String = {
    val sb = new StringBuilder
    doPrettyPrint(tree, ruleNamesList, sb, 0)
    sb.toString
  }

  private def doPrettyPrint(
                             tree: Tree,
                             ruleNamesList: util.List[String],
                             sb: StringBuilder,
                             indent: Int
                           ) {
    val nodeText = Utils.escapeWhitespace(Trees.getNodeText(tree, ruleNamesList), false)

    sb.append("  " * indent).append(nodeText).append('\n')

    for (i <- 0 until tree.getChildCount) {
      doPrettyPrint(tree.getChild(i), ruleNamesList, sb, indent + 1)
    }
  }
}
