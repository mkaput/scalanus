package edu.scalanus

import javax.script.{ScriptEngine, ScriptEngineFactory}

import scala.util.Properties

package object cli {

  def detailedVersion(engine: ScriptEngine): String =
    detailedVersion(engine.getFactory)

  def detailedVersion(factory: ScriptEngineFactory): String = {
    val name = factory.getEngineName
    val version = factory.getEngineVersion
    val vmName = Properties.javaVmName
    val javaVer = Properties.javaVersion
    f"$name $version ($vmName, Java $javaVer)"
  }

}
