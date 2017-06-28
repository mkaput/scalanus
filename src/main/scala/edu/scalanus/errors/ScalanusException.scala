package edu.scalanus.errors

import javax.script.ScriptException

import edu.scalanus.ir.IrCtx
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext

sealed class ScalanusException(
  val detailMessage: String,
  var position: LcfPosition = null,
  cause: Throwable = null
) extends ScriptException(null: String) {

  if (cause != null) initCause(cause)

  override def getMessage: String =
    if (position != null) {
      s"$position: $detailMessage"
    } else {
      detailMessage
    }

  override def getLineNumber: Int = position.lineNumber

  override def getColumnNumber: Int = position.columnNumber

  override def getFileName: String = position.fileName

  def toScriptException: ScriptException = this

}


sealed class ScalanusParseException(
  detailMessage: String,
  position: LcfPosition
) extends ScalanusException(
  detailMessage,
  position
)


sealed class ScalanusCompileException(
  detailMessage: String,
  ctx: ParserRuleContext
) extends ScalanusException(
  detailMessage,
  LcfPosition(ctx)
)


sealed class ScalanusMultiException(
  val exceptions: ScalanusException*
) extends ScalanusException(
  exceptions.map(_.getMessage).mkString("\n"),
  exceptions.head.position
) {

  override def getMessage: String = detailMessage

}

sealed class ScalanusEvalException(detailMessage: String, ctx: IrCtx)
  extends ScalanusException(detailMessage, ctx.position)
