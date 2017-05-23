package edu.scalanus.cli

import javax.script.ScriptEngineManager

import edu.scalanus.ScalanusScriptEngine
import org.jline.reader.{EndOfFileException, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}

import scala.util.Properties

class Shell {

  private val engine = new ScriptEngineManager().getEngineByName("scalanus").asInstanceOf[ScalanusScriptEngine]

  def main(): Unit = {
    welcome()

    val terminal = TerminalBuilder.builder().name("Scalanus")
      .system(true)
      .nativeSignals(true)
      .signalHandler(Terminal.SignalHandler.SIG_IGN)
      .build()

    try {
      val lineReader = LineReaderBuilder.builder()
        .appName("Scalanus")
        .highlighter(new CliHighlighter())
        .build()

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

  private def processLine(line: String): Unit = {
    try {
      val result = engine.eval(line)
      println(result)
    } catch {
      case e: Exception =>
        e.printStackTrace(System.out)
    }
  }

  private def welcome(): Unit = {
    val name = engine.factory.getEngineName
    val version = engine.factory.getEngineVersion
    val vmName = Properties.javaVmName
    val javaVer = Properties.javaVersion
    println(f"Welcome to $name $version ($vmName, Java $javaVer)")
    println("Press ^D or ^C twice to exit.")
  }

}
