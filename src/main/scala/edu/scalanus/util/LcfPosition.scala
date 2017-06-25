package edu.scalanus.util

import org.antlr.v4.runtime.{IntStream, ParserRuleContext}

/** Line-column-file name position in source text. */
case class LcfPosition(
  lineNumber: Int,
  columnNumber: Int = -1,
  fileName: String = null
) {

  if (lineNumber < 0) throw new IllegalArgumentException("lineNumber")
  if (columnNumber < -1) throw new IllegalArgumentException("columnNumber")

  override def toString: String = {
    val sb = new StringBuilder

    if (fileName != null && !fileName.isEmpty) {
      sb.append(fileName).append(':')
    } else {
      sb.append("line ")
    }

    sb.append(lineNumber)

    if (columnNumber != -1) {
      sb.append(':').append(columnNumber)
    }

    sb.toString
  }

}

object LcfPosition {
  def apply(ctx: ParserRuleContext): LcfPosition = {
    val line = ctx.getStart.getLine
    val column = ctx.getStart.getCharPositionInLine
    val fileName = ctx.getStart.getInputStream.getSourceName match {
      case IntStream.UNKNOWN_SOURCE_NAME => null
      case s => s
    }
    LcfPosition(line, column, fileName)
  }
}
