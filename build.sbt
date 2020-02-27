name := "akkaDispatcher"

version := "0.1"

scalaVersion := "2.13.1"


libraryDependencies +=  "org.scalatest" %% "scalatest" % "3.1.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.3" % Test