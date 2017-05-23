package edu.scalanus.cli

import java.io.File

import edu.scalanus.ScalanusScriptEngineFactory
import scopt.OptionParser

object Main extends App {

  val factory = new ScalanusScriptEngineFactory

  val argparser = new OptionParser[ArgConfig]("scalanus") {
    head(detailedVersion(factory))

    arg[File]("file").valueName("<file>")
      .optional()
      .action { (f, c) => c.copy(file = Some(f)) }
      .text("program read from script file")

    opt[Unit]("version")
      .optional()
      .action { (_, c) => c.copy(showVersion = true) }
      .text(s"print the ${factory.getEngineName} version and exit")

    help("help").text("print this help message and exit")

    note("\nif no arguments are passed then repl mode is ran.\n")
  }

  argparser.parse(args, ArgConfig()) match {
    case Some(config) if config.showVersion =>
      println(detailedVersion(factory))

    case Some(config) if config.file.isDefined =>
      new Runner().main(config)

    case Some(config) =>
      new Repl().main(config)
  }

}
