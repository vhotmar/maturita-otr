name := "basic-project"

organization := "example"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.56",
  "org.scodec" % "scodec-bits_2.11" % "1.1.4",
  "org.scodec" % "scodec-core_2.11" % "1.10.3",
  "org.scalaz" %% "scalaz-core" % "7.2.9"
)

initialCommands := "import example._"
