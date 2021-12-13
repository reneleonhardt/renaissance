lazy val renaissanceCore = RootProject(uri("../../renaissance-core"))

lazy val pluginJMXTimers = (project in file("."))
  .settings(
    name := "plugin-jmxmemory",
    version := "0.0.1",
    crossPaths := false,
    autoScalaLibrary := false,
    organization := (renaissanceCore / organization).value,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("org", "renaissance", "plugins", _*) => MergeStrategy.first
      case PathList("org", "renaissance", _*) => MergeStrategy.discard
      case _ => MergeStrategy.singleOrError
    },
    packageOptions := Seq(
      sbt.Package.ManifestAttributes(
        ("Renaissance-Plugin", "org.renaissance.plugins.jmxmemory.Main")
      )
    ),
  )
  .dependsOn(
    renaissanceCore
  )