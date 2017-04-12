name := "basic-project"

organization := "example"

version := "1.0.1-alpha"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.56" % "provided",
  "org.scodec" % "scodec-bits_2.11" % "1.1.4",
  "org.scodec" % "scodec-core_2.11" % "1.10.3",
  "org.scalaz" %% "scalaz-core" % "7.2.9",
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "com.typesafe.akka" %% "akka-agent" % "2.4.17",
  "com.typesafe.akka" %% "akka-remote" % "2.4.17",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.3",
  "org.scaldi" %% "scaldi" % "0.5.8",
  "org.scaldi" %% "scaldi-akka" % "0.5.8",
  "org.typelevel" %% "shapeless-scalaz" % "0.4",
  "joda-time" % "joda-time" % "2.9.9"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

initialCommands := "import example._"
