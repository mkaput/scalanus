package edu.scalanus.cli

import javax.script.{ScriptEngineManager, ScriptException}

import org.jline.reader.{EndOfFileException, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}

class Repl {

  private val engine = new ScriptEngineManager().getEngineByName("scalanus")

  def main(config: ArgConfig): Unit = {
    println("Welcome to " + detailedVersion(engine))
    println("Press ^D or ^C twice to exit.")

    val terminal = TerminalBuilder.builder().name("Scalanus")
      .system(true)
      .nativeSignals(true)
      .signalHandler(Terminal.SignalHandler.SIG_IGN)
      .build()

    try {
      val lineReader = LineReaderBuilder.builder()
        .appName("Scalanus")
        .highlighter(new ReplHighlighter())
        .build()

      var sigint = false

      while (true) {
        try {
          val line = lineReader.readLine("scl> ")

          sigint = false

          processLine(line)
        } catch {
          case _: UserInterruptException =>
            if (sigint) return
            println("Press ^C again to exit...")
            sigint = true

          case _: EndOfFileException => return
        }
      }
    } finally {
      terminal.close()
    }
  }

  private def processLine(line: String): Unit = {
    try {
      val result = engine.eval(line)
      println(result)
    } catch {
      case e: ScriptException => println(e.getMessage)
      case e: Exception => e.printStackTrace(System.out)
    }
  }

}
