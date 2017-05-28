package edu.scalanus.compiler

import edu.scalanus.errors.ScalanusParseException
import edu.scalanus.util.Location
import org.antlr.v4.runtime.{BaseErrorListener, IntStream, RecognitionException, Recognizer}

import scala.collection.mutable.ArrayBuffer

class CompilerParserErrorListener extends BaseErrorListener {

  private val errors: ArrayBuffer[(Location, String)] = ArrayBuffer()

  def foundErrrors: Array[(Location, String)] = errors.toArray

  @throws[ScalanusParseException]
  def validate(): Unit = {
    if (errors.nonEmpty) throw ScalanusParseException(errors.toArray)
  }

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
    errors += ((Location(line, charPositionInLine, sourceName), msg))
  }

}
