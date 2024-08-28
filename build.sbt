
// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.0.5"
ThisBuild / organization     := "edu.duke.cs.apex"

val chiselVersion = "3.5.6"

val beethoven = project in file("./beethoven")

lazy val chipkit = (project in file("."))
  .settings(
    name := "chipkit",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
//      "edu.duke.cs.apex" %% "beethoven-hardware" % "beta.0.0.2"
    ),
    resolvers += ("reposilite-repository-releases" at "http://oak:8080/releases").withAllowInsecureProtocol(true),
    publishTo := Some(("reposilite-repository" at "http://oak:8080/releases/").withAllowInsecureProtocol(true)),
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  ).dependsOn(beethoven)

