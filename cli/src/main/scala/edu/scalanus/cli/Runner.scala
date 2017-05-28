package edu.scalanus.cli

import javax.script.{ScriptContext, ScriptEngine, ScriptEngineManager, ScriptException}

import scala.io.Source

class Runner {

  private val engine = new ScriptEngineManager().getEngineByName("scalanus")

  def main(config: ArgConfig): Unit = {
    val file = config.file.get

    if(!file.exists()) {
      System.err.println(s"file $file does not exist")
      System.exit(1)
    }

    try {
      engine.getContext.setAttribute(ScriptEngine.FILENAME, file.toString, ScriptContext.ENGINE_SCOPE)
      engine.eval(Source.fromFile(file).bufferedReader())
    } catch {
      case e: ScriptException => System.err.println(e.getMessage)
      case e: Exception => e.printStackTrace()
    }
  }

}
