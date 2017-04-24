package edu.scalanus.cli

import edu.scalanus.ScalanusBuildInfo

object Main {
  def main(args: Array[String]): Unit = {
    println(s"Hello Scalanus ${ScalanusBuildInfo.version}")
  }
}
