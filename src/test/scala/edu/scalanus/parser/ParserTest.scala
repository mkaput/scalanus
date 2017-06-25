package edu.scalanus.parser

import java.io.File

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.{CharStream, CommonTokenStream, Parser}

class ParserTest extends ParserTestBase {
  override def basePath: String = "parser/fixtures"

  override protected def lexIncludeHiddenTokens(f: File): Boolean = Array(
    "lexer/comments.lex"
  ).exists(f.toPath.endsWith)

  override protected def createCommonTokenStream(stream: CharStream): CommonTokenStream = {
    val lexer = new ScalanusLexer(stream)
    new CommonTokenStream(lexer)
  }

  override protected def createParseTree(tokenStream: CommonTokenStream): (Parser, ParseTree) = {
    val parser = new ScalanusParser(tokenStream)
    (parser, parser.program())
  }
}
