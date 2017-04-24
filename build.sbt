lazy val commonSettings = Seq(
  organization := "edu.scalanus",
  version := "1.0",
  scalaVersion := "2.12.1"
)

lazy val core = project
  .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,
    name := "scalanus",
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "edu.scalanus",
    buildInfoObject := "ScalanusBuildInfo"
  )

lazy val cli = project
  .settings(
    commonSettings,
    name := "scalanus-cli"
  )
  .dependsOn(core)
