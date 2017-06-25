package edu.scalanus.compiler

import edu.scalanus.errors.ScalanusParseException
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.{BaseErrorListener, IntStream, RecognitionException, Recognizer}

class ParserErrorListener extends BaseErrorListener with ScalanusErrorListener {

  override def syntaxError(
    recognizer: Recognizer[_, _],
    offendingSymbol: scala.Any,
    line: Int,
    charPositionInLine: Int,
    msg: String,
    e: RecognitionException
  ): Unit = {
    val sourceName = recognizer.getInputStream.getSourceName match {
      case IntStream.UNKNOWN_SOURCE_NAME => null
      case s => s
    }
    report(new ScalanusParseException(msg, LcfPosition(line, charPositionInLine, sourceName)))
  }

}
