package edu.scalanus.cli

import edu.scalanus.ScalanusBuildInfo
import edu.scalanus.parser.{ScalanusLexer, ScalanusParser}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

object Main {
  def main(args: Array[String]): Unit = {
    println(s"Hello Scalanus ${ScalanusBuildInfo.version}")

    val input = CharStreams.fromStream(System.in)
    val lexer = new ScalanusLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new ScalanusParser(tokens)
    val tree = parser.init()
    println(tree.toStringTree(parser))
  }
}
