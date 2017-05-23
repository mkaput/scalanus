package edu.scalanus.cli

import edu.scalanus.parser.ScalanusLexerUtil._
import edu.scalanus.parser.{ScalanusLexer, ThrowingErrorListener}
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.{CharStreams, Token}
import org.jline.reader.{Highlighter, LineReader}
import org.jline.utils.{AttributedString, AttributedStringBuilder, AttributedStyle}

class CliHighlighter extends Highlighter {
  private val KEYWORD_STYLE = AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT).bold()
  private val COMMENT_STYLE = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
  private val STRING_STYLE = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
  private val NUMBER_STYLE = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)

  override def highlight(reader: LineReader, buffer: String): AttributedString = {
    val lexer = new ScalanusLexer(CharStreams.fromString(buffer))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingErrorListener)

    val asb = new AttributedStringBuilder()

    try {
      var tok = lexer.nextToken
      while (tok.getType != Token.EOF) {
        val style = if (isKeyword(tok) || isBoolLiteral(tok)) {
          KEYWORD_STYLE
        } else if (isComment(tok)) {
          COMMENT_STYLE
        } else if (isStringLiteral(tok) || isCharLiteral(tok)) {
          STRING_STYLE
        } else if (isIntLiteral(tok) || isFloatLiteral(tok)) {
          NUMBER_STYLE
        } else {
          AttributedStyle.DEFAULT
        }

        asb.append(tok.getText, style)

        tok = lexer.nextToken
      }
    } catch {
      case _: ParseCancellationException | _: StringIndexOutOfBoundsException =>
        asb.append(buffer.substring(asb.length()), AttributedStyle.DEFAULT)
    }

    asb.toAttributedString
  }
}
