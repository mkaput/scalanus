package edu.scalanus.runner

import javax.script.ScriptEngineManager

object Main extends App {
  val scriptEngineManager = new ScriptEngineManager()
  val scalanus = scriptEngineManager.getEngineByName("Scalanus")

  Console.println(s"Hello Scalanus ${scalanus.getFactory.getEngineVersion} from runner")
  Console.println(args.mkString(", "))
}
