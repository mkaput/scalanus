package edu.scalanus.compiler

import java.io.Reader

import edu.scalanus.ScalanusScriptEngine
import edu.scalanus.ir.IrNode
import edu.scalanus.parser.{ScalanusLexer, ScalanusParser}
import org.antlr.v4.runtime.{CharStream, CharStreams, CommonTokenStream}

class ScalanusCompiler(val engine: ScalanusScriptEngine) {

  def compile(script: String, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromString(script, name))

  def compile(reader: Reader, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromReader(reader, name))

  private def compile(charStream: CharStream): ScalanusCompiledScript = {
    val program = parseProgram(charStream)
    val ir = compileProgram(program)
    new ScalanusCompiledScript(ir, engine)
  }

  private def parseProgram(charStream: CharStream): ScalanusParser.ProgramContext = {
    val errorListener = new CompilerParserErrorListener

    val lexer = new ScalanusLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(errorListener)

    val commonTokenStream = new CommonTokenStream(lexer)

    val parser = new ScalanusParser(commonTokenStream)
    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)

    val program = parser.program()

    errorListener.validate()

    program
  }

  private def compileProgram(program: ScalanusParser.ProgramContext): IrNode = {
    val visitor = new CompilerVisitor()
    visitor.visitProgram(program)
  }

}
