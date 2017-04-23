lazy val commonSettings = Seq(
  organization := "edu.scalanus",
  version := "1.0",
  scalaVersion := "2.12.1"
)

lazy val core = project
  .settings(
    commonSettings,
    name := "scalanus"
  )

lazy val repl = project
  .settings(
    commonSettings,
    name := "scalanus-repl"
  )
  .dependsOn(core)

lazy val runner = project
  .settings(
    commonSettings,
    name := "scalanus-runner"
  )
  .dependsOn(core)
