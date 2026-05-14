ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "uvg-scala",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
  )
