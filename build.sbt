import akka.grpc.gen.scaladsl.play.{ PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator }

organization in ThisBuild := "org.sigurdthor"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `book-shelf` = (project in file("."))
  .aggregate(`book-api`, `book-impl`)

lazy val `book-api` = (project in file("book-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
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
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq( AkkaGrpc.Server, AkkaGrpc.Client ),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator
  )
  .dependsOn(`book-api`)

lagomKafkaEnabled in ThisBuild := false
