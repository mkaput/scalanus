package edu.scalanus.cli

import javax.script.ScriptEngineManager

object Main {
  def main(args: Array[String]): Unit = {
    val engine = new ScriptEngineManager().getEngineByName("scalanus")
    println(s"Hello Scalanus ${engine.getFactory.getEngineVersion}!")
  }
}
