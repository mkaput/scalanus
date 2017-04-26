package edu.scalanus.parser

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.{CharStream, CommonTokenStream, Parser}

class ParserSpec extends ParserSpecBase {
  override def basePath: String = "parser/fixtures"

  override protected def createCommonTokenStream(stream: CharStream): CommonTokenStream = {
    val lexer = new ScalanusLexer(stream)
    new CommonTokenStream(lexer)
  }

  override protected def createParseTree(tokenStream: CommonTokenStream): (Parser, ParseTree) = {
    val parser = new ScalanusParser(tokenStream)
    (parser, parser.init())
  }
}
