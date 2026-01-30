name := "PingPongActors"

version := "0.1.0"

scalaVersion := "3.7.0"

lazy val akkaVersion = "2.9.8"
lazy val cinnamonVersion = "2.22.0"

// Cinnamon credentials repository
credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/pass/XXXXX/commercial-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.12",
  
  // Cinnamon dependencies
  "com.lightbend.cinnamon" %% "cinnamon-akka" % cinnamonVersion,
  "com.lightbend.cinnamon" %% "cinnamon-prometheus" % cinnamonVersion,
  "com.lightbend.cinnamon" %% "cinnamon-prometheus-httpserver" % cinnamonVersion
)

// Enable Cinnamon
enablePlugins(Cinnamon)

// Cinnamon settings
cinnamon in run := true
cinnamon in test := true
