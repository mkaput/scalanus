package edu.scalanus.runner

import edu.scalanus.Hello

object Main extends App {
  Console.println(s"Hello ${Hello.hello} from runner")
  Console.println(args.mkString(", "))
}
