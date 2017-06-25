package edu.scalanus.cli

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.{BaseErrorListener, RecognitionException, Recognizer}

object HighlighterErrorListener extends BaseErrorListener {

  @throws[ParseCancellationException]
  override def syntaxError(
    recognizer: Recognizer[_, _],
    offendingSymbol: scala.Any,
    line: Int,
    charPositionInLine: Int,
    msg: String,
    e: RecognitionException
  ): Unit =
    throw new ParseCancellationException(s"line $line:$charPositionInLine $msg")

}
