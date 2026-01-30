name := "PingPongActors"

version := "0.1.0"

scalaVersion := "3.7.0"

// Using Apache Pekko instead of Akka for better compatibility
lazy val pekkoVersion = "1.1.2"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.12"
)

// Uncomment the following lines when Cinnamon credentials are configured
// Note: Cinnamon supports Akka. For Pekko, you would need Pekko-specific monitoring solutions
// See README.md for Akka + Cinnamon setup instructions
/*
lazy val akkaVersion = "2.9.8"
lazy val cinnamonVersion = "2.22.0"

credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
resolvers += Resolver.url("lightbend-commercial", 
  url("https://repo.lightbend.com/pass/XXXXX/commercial-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.lightbend.cinnamon" %% "cinnamon-akka" % cinnamonVersion,
  "com.lightbend.cinnamon" %% "cinnamon-prometheus" % cinnamonVersion,
  "com.lightbend.cinnamon" %% "cinnamon-prometheus-httpserver" % cinnamonVersion
)

enablePlugins(Cinnamon)

cinnamon in run := true
cinnamon in test := true
*/


