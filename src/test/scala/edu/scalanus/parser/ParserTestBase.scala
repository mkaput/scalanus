package edu.scalanus.parser

import java.io.File

import edu.scalanus.FileFixtureTest
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree.ParseTree
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters._

abstract class ParserTestBase extends FunSuite with Matchers with FileFixtureTest {

  protected def createCommonTokenStream(stream: CharStream): CommonTokenStream

  protected def createParseTree(tokenStream: CommonTokenStream): (Parser, ParseTree)

  protected def lexIncludeHiddenTokens(f: File): Boolean = false

  protected override def fixtureExtensions = Array("lex", EXT)

  fixtures foreach (f => {
    val isLex = f.getName endsWith ".lex"

    test(getFixtureName(f)) {
      if (isLex) {
        doTestLex(f)
      } else {
        doTestParser(f)
      }
    }
  })

  private def doTestLex(f: File) {
    val stream = CharStreams.fromPath(f.toPath)
    val tokens = createCommonTokenStream(stream)
    tokens.fill()

    val recognizer = tokens.getTokenSource match {
      case lexer: Lexer => lexer
      case _ => null
    }

    val found = tokens.getTokens().asScala
      .withFilter(token => token.getChannel == 0 || lexIncludeHiddenTokens(f))
      .map {
        case ctok: CommonToken => ctok.toString(recognizer)
        case tok => tok.toString
      }
      .mkString("\n")

    val expected = getOrCreateResults(f, found)

    found shouldEqual expected
  }

  private def doTestParser(f: File) {
    val stream = CharStreams.fromPath(f.toPath)
    val tokens = createCommonTokenStream(stream)
    val (parser, tree) = createParseTree(tokens)
    val actual = TreePrettyPrinter(tree, parser)
    val results = getOrCreateResults(f, actual)
    actual shouldEqual results
  }

}
