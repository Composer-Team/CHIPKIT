
// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.0.0"
ThisBuild / organization     := "edu.duke.cs.apex"

val chiselVersion = "3.5.6"

lazy val chipkit = (project in file("."))
  .settings(
    name := "chipkit",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.duke.cs.apex" %% "rocketchip-rocketchip-fork" % "0.1.14",
    ),
    resolvers += ("reposilite-repository-releases" at "http://oak:8080/releases").withAllowInsecureProtocol(true),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
//  .dependsOn(composer)


