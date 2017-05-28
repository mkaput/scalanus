package edu.scalanus.compiler

import java.io.Reader

import edu.scalanus.ScalanusScriptEngine
import org.antlr.v4.runtime.{CharStream, CharStreams}

class ScalanusCompiler(val engine: ScalanusScriptEngine) {

  def compile(script: String, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromString(script, name), name)

  def compile(reader: Reader, name: String): ScalanusCompiledScript =
    compile(CharStreams.fromReader(reader, name), name)

  def compile(charStream: CharStream, name: String): ScalanusCompiledScript = ???

}
