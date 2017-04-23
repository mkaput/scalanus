package edu.scalanus.repl

import edu.scalanus.Hello

object Main extends App {
  Console.println(s"Hello ${Hello.hello} from repl")
  Console.println(args.mkString(", "))
}
