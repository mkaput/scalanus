package edu.scalanus.compiler

import edu.scalanus.errors.ScalanusException
import edu.scalanus.parser.ScalanusParser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

import scala.collection.JavaConverters._

object LiteralParser {

  /** Assumes lexically valid input */
  def parse(ctx: ScalanusParser.LiteralContext): Any = {
    if (ctx.unit != null) return ()

    val child = getFirstToken(ctx)
    val text = child.getText
    child.getSymbol.getType match {
      case ScalanusParser.CHAR_LIT => parseChar(text)
      case ScalanusParser.STRING_LIT => parseString(text)
      case ScalanusParser.FLOAT_LIT => parseFloat(text)
      case ScalanusParser.INT_LIT => parseInt(text)
      case ScalanusParser.BOOL_LIT => parseBool(text)
    }
  }

  /** Assumes lexically valid input */
  def parseChar(text: String): Any =
    unescape(text.stripPrefix("'").stripSuffix("'")).charAt(0)

  /** Assumes lexically valid input */
  def parseString(text: String): Any =
    unescape(text.stripPrefix("\"").stripSuffix("\""))

  /** Assumes lexically valid input */
  def parseFloat(text: String): Any =
    try {
      text.filter(_ != '_').toDouble
    } catch {
      case _: NumberFormatException =>
        throw new ScalanusException(s"invalid float literal $text")
    }

  /** Assumes lexically valid input */
  def parseInt(text: String): Any =
    try {
      val filtered = text.filter(_ != '_')
      if (filtered.startsWith("0b")) {
        Integer.parseInt(filtered.substring(2), 2)
      } else if (filtered.startsWith("0o")) {
        Integer.parseInt(filtered.substring(2), 8)
      } else if (filtered.startsWith("0x")) {
        Integer.parseInt(filtered.substring(2), 16)
      } else {
        filtered.toInt
      }
    } catch {
      case _: NumberFormatException =>
        throw new ScalanusException(s"invalid int literal $text")
    }

  def parseBool(text: String): Any = text match {
    case "True" => true
    case "False" => false
  }

  def unescape(raw: String): String = {
    def throwIllegal =
      throw new ScalanusException(s"Illegal escape in string: '$raw'")

    val sb = new StringBuilder(raw.length)

    var i = 0
    while (i < raw.length) {
      raw.charAt(i) match {
        case '\\' =>
          i += 1
          if (i >= raw.length) throwIllegal
          raw.charAt(i) match {
            case '\\' => sb.append('\\')
            case 'n' => sb.append('\n')
            case 'r' => sb.append('\r')
            case 't' => sb.append('\t')
            case '0' => sb.append('\u0000')
            case '\'' => sb.append('\'')
            case '"' => sb.append('"')

            case 'x' =>
              if (i + 2 >= raw.length) throwIllegal
              val codeStr = raw.substring(i + 1, i + 3)
              val code = try {
                Integer.parseUnsignedInt(codeStr, 16)
              } catch {
                case _: NumberFormatException => throwIllegal
              }
              sb.append(code.toChar)
              i += 2

            case 'u' =>
              if (i + 2 >= raw.length) throwIllegal
              if (raw.charAt(i + 1) != '{') throwIllegal
              val j = raw.indexOf('}', i + 2)
              if (j < 0 || j - i > 8) throwIllegal
              val codeStr = raw.substring(i + 2, j)
              val code = try {
                Integer.parseUnsignedInt(codeStr, 16)
              } catch {
                case _: NumberFormatException => throwIllegal
              }
              sb.append(code.toChar)
              i = j

            case _ => throwIllegal
          }
        case c => sb.append(c)
      }
      i += 1
    }

    sb.toString
  }

  private def getFirstToken(ctx: ParserRuleContext): TerminalNode =
    ctx.children.asScala.collectFirst { case n: TerminalNode => n }.get
}
