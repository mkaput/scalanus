package edu.scalanus.compiler

import java.io.Reader

import edu.scalanus.ScalanusScriptEngine
import edu.scalanus.parser.{ScalanusLexer, ScalanusParser}
import org.antlr.v4.runtime.{CharStream, CharStreams, CommonTokenStream}

class ScalanusCompiler(val engine: ScalanusScriptEngine) {

  def compile(script: String, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromString(script, name))

  def compile(reader: Reader, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromReader(reader, name))

  private def compile(charStream: CharStream): ScalanusCompiledScript = {
    val parserErrorListener = new ParserErrorListener

    val lexer = new ScalanusLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(parserErrorListener)

    val commonTokenStream = new CommonTokenStream(lexer)

    val parser = new ScalanusParser(commonTokenStream)
    parser.removeErrorListeners()
    parser.addErrorListener(parserErrorListener)

    val program = parser.program()

    parserErrorListener.validate()

    val errorListener = new CompilerErrorListener
    val visitor = new CompilerVisitor(errorListener)

    val maybeIr = visitor.visitProgram(program)

    errorListener.validate()

    // If there were no compilation errors,
    // then we must have some generated IR.
    val ir = maybeIr.get

    new ScalanusCompiledScript(ir, engine)
  }

}
