package edu.scalanus.cli

import javax.script.ScriptEngineManager

import scala.io.Source

class Runner {

  private val engine = new ScriptEngineManager().getEngineByName("scalanus")

  def main(config: ArgConfig): Unit = {
    val file = config.file.get
    engine.eval(Source.fromFile(file).bufferedReader())
  }

}
