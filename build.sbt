organization := "com.pagerduty"

name := "eris-core"

scalaVersion := "2.10.4"

publishArtifact in Test := true

libraryDependencies ++= Seq(
  "com.netflix.astyanax" % "astyanax-cassandra" % "3.6.0" exclude("org.slf4j", "slf4j-log4j12"),
  "com.netflix.astyanax" % "astyanax-core" % "3.6.0" exclude("org.slf4j", "slf4j-log4j12"),
  "com.netflix.astyanax" % "astyanax-thrift" % "3.6.0",
  "com.google.guava" % "guava" % "18.0",
  "org.apache.thrift" % "libthrift" % "0.9.1",
  "com.eaio.uuid" % "uuid" % "3.2",
  "org.apache.cassandra" % "cassandra-all" % "2.0.12" exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.cassandra" % "cassandra-thrift" % "2.0.12",
  "org.slf4j" % "slf4j-api" % "1.7.12")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.scalatest" %% "scalatest" % "2.2.4" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.2" % Test)
