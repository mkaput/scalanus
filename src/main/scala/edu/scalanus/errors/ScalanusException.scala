package edu.scalanus.errors

import javax.script.ScriptException

import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.{IntStream, ParserRuleContext}

sealed abstract class ScalanusException(
  cause: Throwable = null
) extends ScriptException(null: String) {

  if (cause != null) initCause(cause)

  val detailMessage: String

  val position: LcfPosition

  override def getMessage: String = s"$position: $detailMessage"

  override def getLineNumber: Int = position.lineNumber

  override def getColumnNumber: Int = position.columnNumber

  override def getFileName: String = position.fileName

  def toScriptException: ScriptException = this

}


case class ScalanusParseException(detailMessage: String, position: LcfPosition) extends ScalanusException

case class ScalanusCompileException(detailMessage: String, ctx: ParserRuleContext) extends ScalanusException {
  override val position: LcfPosition = {
    val line = ctx.getStart.getLine
    val column = ctx.getStart.getCharPositionInLine
    val fileName = ctx.getStart.getInputStream.getSourceName match {
      case IntStream.UNKNOWN_SOURCE_NAME => null
      case s => s
    }
    LcfPosition(line, column, fileName)
  }
}


case class ScalanusMultiException(exceptions: ScalanusException*) extends ScalanusException {

  require(exceptions.nonEmpty)

  override lazy val detailMessage: String = exceptions.map(_.getMessage).mkString("\n")

  override val position: LcfPosition = exceptions.head.position

  override def getMessage: String = detailMessage

}
