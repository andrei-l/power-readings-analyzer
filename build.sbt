
lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `power-readings-analyzer` = project in file(".")

name := "power-readings-analyzer"
version := "1.0"
scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  scalaCsv,
  scalaTest
)
