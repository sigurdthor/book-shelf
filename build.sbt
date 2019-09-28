import akka.grpc.gen.scaladsl.play.{ PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator }

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

val sangria = "org.sangria-graphql" %% "sangria" % "1.4.2"
val sangriaCirce = "org.sangria-graphql" %% "sangria-circe" % "1.2.1"

lazy val `book-shelf` = (project in file("."))
  .aggregate(`book-api`, `book-impl`, `graphql-gateway`)

lagomServiceEnableSsl in ThisBuild := true

lazy val `book-api` = (project in file("book-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      cats
    )
  )

lazy val `book-impl` = (project in file("book-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      zio,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq( AkkaGrpc.Server),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,
    lagomServiceHttpsPort := 8443
  )
  .dependsOn(`book-api`)

lazy val `graphql-gateway` = (project in file("graphql-gateway"))
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    libraryDependencies ++= Seq(
      http4s,
      http4sCirce,
      http4sDsl,
      circe,
      circeOptics,
      sangria,
      sangriaCirce
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client),
    akkaGrpcExtraGenerators in Compile += PlayScalaClientCodeGenerator
  )

lagomKafkaEnabled in ThisBuild := false
lagomServiceLocatorEnabled in ThisBuild := false