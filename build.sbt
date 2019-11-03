import akka.grpc.gen.scaladsl.play.{PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator}
import com.typesafe.sbt.packager.docker.Cmd

organization in ThisBuild := "org.sigurdthor"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val zio = "dev.zio" %% "zio" % "1.0.0-RC12-1"
val cats = "org.typelevel" %% "cats-core" % "2.0.0"
val Http4sVersion = "0.20.4"
val CirceVersion = "0.11.1"

val http4s = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
val circe = "io.circe" %% "circe-generic" % CirceVersion
val circeOptics = "io.circe" %% "circe-optics" % "0.9.3"
val alpnVersion = "2.0.9"

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"

val discovery = "com.typesafe.akka" %% "akka-discovery" % "2.5.25"
val discoveryLagom = "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.5.3"
val discoveryKubernetes = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.3"

val sangria = "org.sangria-graphql" %% "sangria" % "1.4.2"
val sangriaCirce = "org.sangria-graphql" %% "sangria-circe" % "1.2.1"

val agensGraph = "net.bitnine" % "agensgraph-jdbc" % "1.4.2"

lazy val `book-shelf` = (project in file("."))
  .aggregate(`book-api`, `book-impl`, `graphql-gateway`, `recommendation-api`, `recommendation-impl`)

lagomServiceEnableSsl in ThisBuild := true

lazy val `book-api` = (project in file("book-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      cats
    )
  )

lazy val `recommendation-api` = (project in file("recommendation-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      cats
    )
  )

lazy val `book-impl` = (project in file("book-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin, DockerPlugin, JavaAppPackaging)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      zio,
      macwire,
      scalaTest,
      discoveryLagom,
      discoveryKubernetes
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "chmod -R u+rwx,g+rwx,o+rwx /opt/docker"
      ),
      Cmd("USER", "1001")
    ),
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,
    lagomServiceHttpsPort := 8443,
    lagomServiceAddress := "0.0.0.0"
  )
  .dependsOn(`book-api`)

lazy val `recommendation-impl` = (project in file("recommendation-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin, DockerPlugin, JavaAppPackaging)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaClient,
      lagomScaladslTestKit,
      zio,
      macwire,
      scalaTest,
      agensGraph,
      discoveryLagom,
      discoveryKubernetes
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "chmod -R u+rwx,g+rwx,o+rwx /opt/docker"
      ),
      Cmd("USER", "1001")
    ),
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,
    lagomServiceHttpsPort := 8445,
    lagomServiceAddress := "0.0.0.0"
  )
  .dependsOn(`recommendation-api`, `book-api`)

lazy val `graphql-gateway` = (project in file("graphql-gateway"))
  .enablePlugins(AkkaGrpcPlugin, JavaAppPackaging, DockerPlugin, JavaAgent)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnVersion % "runtime",
    libraryDependencies ++= Seq(
      http4s,
      http4sCirce,
      http4sDsl,
      circe,
      circeOptics,
      sangria,
      sangriaCirce,
      discovery,
      discoveryKubernetes
    )
  )
  .settings(
    assemblyMergeStrategy in assembly := {
      case PathList(ps@_*) if ps.last contains "reference-overrides.conf" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client),
    akkaGrpcExtraGenerators in Compile += PlayScalaClientCodeGenerator
  )

//lagomServiceLocatorEnabled in ThisBuild := false