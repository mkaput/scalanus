package edu.scalanus.parser

import org.antlr.v4.runtime.Token

object ScalanusLexerUtil {

  private val _keywordTokens = Set(
    ScalanusLexer.AND,
    ScalanusLexer.AS,
    ScalanusLexer.BREAK,
    ScalanusLexer.CASE,
    ScalanusLexer.CLASS,
    ScalanusLexer.CONST,
    ScalanusLexer.CONTINUE,
    ScalanusLexer.DO,
    ScalanusLexer.ELSE,
    ScalanusLexer.ENUM,
    ScalanusLexer.FN,
    ScalanusLexer.FOR,
    ScalanusLexer.IF,
    ScalanusLexer.IMPORT,
    ScalanusLexer.IN,
    ScalanusLexer.LOOP,
    ScalanusLexer.MOD,
    ScalanusLexer.OR,
    ScalanusLexer.RETURN,
    ScalanusLexer.TRAIT,
    ScalanusLexer.TYPE,
    ScalanusLexer.UNDERSCORE,
    ScalanusLexer.WHILE,
    ScalanusLexer.YIELD
  )

  private val _commentTokens = Set(
    ScalanusLexer.BLOCK_COMMENT,
    ScalanusLexer.LINE_COMMENT
  )

  def isKeyword(token: Token): Boolean = _keywordTokens.contains(token.getType)

  def isComment(token: Token): Boolean = _commentTokens.contains(token.getType)

  def isBoolLiteral(token: Token): Boolean = token.getType == ScalanusLexer.BOOL_LIT

  def isCharLiteral(token: Token): Boolean = token.getType == ScalanusLexer.CHAR_LIT

  def isStringLiteral(token: Token): Boolean = token.getType == ScalanusLexer.STRING_LIT

  def isIntLiteral(token: Token): Boolean = token.getType == ScalanusLexer.INT_LIT

  def isFloatLiteral(token: Token): Boolean = token.getType == ScalanusLexer.FLOAT_LIT

  def isIdentifier(token: Token): Boolean = token.getType == ScalanusLexer.IDENT
 
}
