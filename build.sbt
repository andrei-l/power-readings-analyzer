
lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
lazy val catsCore = "org.typelevel" %% "cats-core" % "1.1.0"
lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `power-readings-analyzer` = project in file(".")

name := "power-readings-analyzer"
version := "1.0"
scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  scalaCsv,
  catsCore,
  logback,
  scalaLogging,

  scalaTest
)

oneJarSettings

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")