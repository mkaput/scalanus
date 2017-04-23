package edu.scalanus.repl

import edu.scalanus.ScalanusBuildInfo

object Main extends App {
  Console.println(s"Hello Scalanus ${ScalanusBuildInfo.toString} from repl")
  Console.println(args.mkString(", "))
}
