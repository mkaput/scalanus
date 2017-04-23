package edu.scalanus.runner

import edu.scalanus.ScalanusBuildInfo

object Main extends App {
  Console.println(s"Hello Scalanus ${ScalanusBuildInfo.toString} from runner")
  Console.println(args.mkString(", "))
}
