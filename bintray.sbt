
bintrayOrganization := Some("pagerduty")

bintrayRepository := "oss-maven"

licenses += ("BSD New", url("https://opensource.org/licenses/BSD-3-Clause"))

publishMavenStyle := true

pomExtra := (
  <url>https://github.com/PagerDuty/eris-core</url>
  <scm>
    <url>git@github.com:PagerDuty/eris-core.git</url>
    <connection>scm:git:git@github.com:PagerDuty/eris-core.git</connection>
  </scm>
  <developers>
    <developer>
      <id>lexn82</id>
      <name>Aleksey Nikiforov</name>
      <url>https://github.com/lexn82</url>
    </developer>
  </developers>)
