package edu.scalanus.cli

import javax.script.ScriptEngineManager

import org.jline.reader.impl.DefaultHighlighter
import org.jline.reader.{EndOfFileException, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}

object Shell {

  def shellMain(): Unit = {
    println("Press ^D or ^C twice to exit.")

    val terminal = TerminalBuilder.builder().name("Scalanus")
      .system(true)
      .nativeSignals(true)
      .signalHandler(Terminal.SignalHandler.SIG_IGN)
      .build()

    try {
      val lineReader = LineReaderBuilder.builder()
        .appName("Scalanus")
        .highlighter(new DefaultHighlighter())
        .build()

      val engine = new ScriptEngineManager().getEngineByName("scalanus")

      var sigint = false

      while (true) {
        try {
          val line = lineReader.readLine("scl> ")

          sigint = false

          processLine(line)
        } catch {
          case _: UserInterruptException =>
            if (!sigint) {
              println("Press ^C again to exit...")
              sigint = true
            } else {
              return
            }

          case _: EndOfFileException => return
        }
      }
    } finally {
      terminal.close()
    }
  }

  def processLine(line: String): Unit = {
    println(line)
  }

}
