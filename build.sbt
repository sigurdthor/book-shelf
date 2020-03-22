import com.typesafe.sbt.packager.docker.Cmd
import sbt.Keys.libraryDependencies

organization in ThisBuild := "org.sigurdthor"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.1"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
val zio = "dev.zio" %% "zio" % "1.0.0-RC18"
val catz = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12"
val cats = "org.typelevel" %% "cats-core" % "2.1.1"
val Http4sVersion = "0.21.1"
val CirceVersion = "0.13.0"

val http4s = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

val circe = "io.circe" %% "circe-generic" % CirceVersion
val circeOptics = "io.circe" %% "circe-optics" % "0.13.0"

val caliban = "com.github.ghostdogpr" %% "caliban" % "0.7.1"
val calibanHttp4s = "com.github.ghostdogpr" %% "caliban-http4s" % "0.7.1"
val calibanCats = "com.github.ghostdogpr" %% "caliban-cats" % "0.7.1"

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"

val discovery = "com.typesafe.akka" %% "akka-discovery" % "2.6.4"
val discoveryLagom = "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.6.1"
val discoveryKubernetes = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.5"

val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.12.3"
val logstage = "io.7mind.izumi" %% "logstage-core" % "0.10.2"

val grpcVersion = "1.28.0"

lazy val `book-shelf` = (project in file("."))
  .aggregate(`book-api`, `book-impl`, `graphql-gateway`)

val commonSettings = Def.settings(
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-explaintypes",
    "-unchecked",
    "-encoding",
    "UTF-8",
    "-language:higherKinds",
    "-language:existentials",
    "-Xlint:-infer-any,_",
    "-Ywarn-value-discard",
    "-Ywarn-numeric-widen",
    "-Ywarn-extra-implicit",
    "-Ywarn-unused:_"
  )
)

lazy val `book-api` = (project in file("book-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      cats
    )
  )

lazy val `book-impl` = (project in file("book-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      logstage,
      zio,
      macwire,
      scalaTest,
      discoveryLagom,
      discoveryKubernetes
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(commonSettings)
  .settings(
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "chmod -R u+rwx,g+rwx,o+rwx /opt/docker"
      ),
      Cmd("USER", "1001")
    ),
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value,
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc" % "grpc-netty" % grpcVersion
    )
  )
  .dependsOn(`book-api`)



lazy val `graphql-gateway` = (project in file("graphql-gateway"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      http4s,
      http4sCirce,
      http4sDsl,
      logstage,
      catz,
      zio,
      pureconfig,
      circe,
      circeOptics,
      caliban,
      calibanHttp4s,
      discovery,
      discoveryKubernetes
    )
  )
  .settings(commonSettings)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value,
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc" % "grpc-netty" % grpcVersion
    )
  )

//lagomServiceLocatorEnabled in ThisBuild := false