organization := "com.pagerduty"

name := "eris-core"

crossScalaVersions := Seq("2.10.4", "2.11.7")

publishArtifact in Test := true

// Prevents logging configuration from being included in the test jar.
mappings in (Test, packageBin) ~= { _.filterNot(_._2.endsWith("logback-test.xml")) }

// Dependencies in this configuration are not exported.
ivyConfigurations += config("transient").hide

fullClasspath in Test ++= update.value.select(configurationFilter("transient"))

lazy val root = (project in file(".")).
  configs(IntegrationTest extend (Test)).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      "com.netflix.astyanax" % "astyanax-cassandra" % "3.6.0",
      "com.netflix.astyanax" % "astyanax-core" % "3.6.0",
      "com.netflix.astyanax" % "astyanax-thrift" % "3.6.0",
      "com.google.guava" % "guava" % "18.0",
      "org.apache.thrift" % "libthrift" % "0.9.1",
      "com.eaio.uuid" % "uuid" % "3.2",
      "org.apache.cassandra" % "cassandra-all" % "2.0.12",
      "org.apache.cassandra" % "cassandra-thrift" % "2.0.12",
      "org.slf4j" % "slf4j-api" % "1.7.12").map(
      _.exclude("org.slf4j", "slf4j-log4j12")).map(
      _.exclude("log4j", "log4j")).map(
      _.exclude("junit", "junit"))
  ).
  settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.13" % "transient",
      "org.scalatest" %% "scalatest" % "2.2.4" % "it,test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "it,test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "it,test")
  )

