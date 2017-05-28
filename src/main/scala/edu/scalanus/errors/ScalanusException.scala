package edu.scalanus.errors

import javax.script.ScriptException

import edu.scalanus.util.Location

sealed trait ScalanusException extends Exception {
  def toScriptException: ScriptException
}

case class ScalanusScriptException(
  message: String,
  location: Location = null,
  cause: Throwable = null
) extends ScriptException(
  message,
  if (location != null) location.fileName else null,
  if (location != null) location.lineNumber else -1,
  if (location != null) location.columnNumber else -1
) with ScalanusException {

  if (cause != null) initCause(cause)

  override def toScriptException: ScriptException = this

}

case class ScalanusParseException(
  errors: Array[(Location, String)]
) extends ScriptException(
  errors.headOption.map(_._2).orNull,
  errors.headOption.map(_._1.fileName).orNull,
  errors.headOption.map(_._1.lineNumber).getOrElse(-1),
  errors.headOption.map(_._1.columnNumber).getOrElse(-1)
) with ScalanusException {

  override def getMessage: String =
    errors.map { case (loc, msg) => f"$msg ($loc)" }.mkString("\n")

  override def toScriptException: ScriptException = this

}
