package edu.scalanus

import javax.script.ScriptException

sealed abstract class ScalanusException(
  val message: String,
  val fileName: Option[String] = None,
  val line: Option[Int] = None,
  val column: Option[Int] = None,
  val cause: Throwable = null
) extends Exception(message, cause) {

  def intoScriptException(): ScriptException = {
    val ex = new ScriptException(getMessage, fileName.orNull, line.getOrElse(-1), column.getOrElse(-1))
    if (cause != null) ex.initCause(cause)
    ex
  }

}
