lazy val commonSettings = Seq(
  organization := "edu.scalanus",
  version := "1.0",
  scalaVersion := "2.12.1",

  compileOrder := CompileOrder.JavaThenScala
)

lazy val core = project
  .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,

    name := "scalanus",

    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "edu.scalanus",
    buildInfoObject := "ScalanusBuildInfo",

    antlr4Settings,
    antlr4Version in Antlr4 := "4.7",
    antlr4PackageName in Antlr4 := Some("edu.scalanus.parser"),
    antlr4GenListener in Antlr4 := true,
    antlr4GenVisitor in Antlr4 := true,

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )

lazy val cli = project
  .settings(
    commonSettings,

    name := "scalanus-cli"
  )
  .dependsOn(core)
