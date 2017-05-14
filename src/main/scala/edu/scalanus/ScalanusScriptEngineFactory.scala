package edu.scalanus

import java.util.{List => JavaList}
import javax.script.{ScriptEngine, ScriptEngineFactory}

import scala.collection.JavaConverters._

class ScalanusScriptEngineFactory extends ScriptEngineFactory {
  private val EXTENSIONS = List("scl")
  private val MIME_TYPES = List("application/x-scalanus")
  private val NAMES = List("scalanus", "Scalanus")

  override def getEngineName: String = "Scalanus Engine"

  override def getEngineVersion: String = "1.0"

  override def getExtensions: JavaList[String] = EXTENSIONS.asJava

  override def getLanguageName: String = "scalanus"

  override def getLanguageVersion: String = "1.0"

  override def getMethodCallSyntax(obj: String, m: String, args: String*): String = {
    if (m == null || m.isEmpty) return ""
    val sb = new StringBuilder()
    if (obj != null && !obj.isEmpty) sb.append(obj).append('.')
    sb.append(m)
    sb.append(args.mkString("(", ", ", ")"))
    sb.mkString
  }

  override def getMimeTypes: JavaList[String] = MIME_TYPES.asJava

  override def getNames: JavaList[String] = NAMES.asJava

  override def getOutputStatement(toDisplay: String): String = s"IO.print($toDisplay)"

  override def getParameter(key: String): AnyRef = key match {
    case ScriptEngine.ENGINE => getEngineName
    case ScriptEngine.ENGINE_VERSION => getEngineVersion
    case ScriptEngine.NAME => getEngineName
    case ScriptEngine.LANGUAGE => getLanguageName
    case ScriptEngine.LANGUAGE_VERSION => getLanguageVersion
    case "THREADING" => null // we are not thread-safe
    case _ => null
  }

  override def getProgram(statements: String*): String = statements.mkString("", ";\n", ";")

  override def getScriptEngine: ScriptEngine = new ScalanusScriptEngine(this)
}
