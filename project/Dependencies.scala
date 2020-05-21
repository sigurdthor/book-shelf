import sbt._

object Dependencies {

  val Http4sVersion = "0.21.4"
  val CirceVersion = "0.13.0"
  val calibanVersion = "0.8.0"

  val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
  val zio = "dev.zio" %% "zio" % "1.0.0-RC19"
  val catz = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC14"
  val cats = "org.typelevel" %% "cats-core" % "2.1.1"

  val http4s = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  val circe = "io.circe" %% "circe-generic" % CirceVersion
  val circeOptics = "io.circe" %% "circe-optics" % CirceVersion

  val caliban = "com.github.ghostdogpr" %% "caliban" % calibanVersion
  val calibanHttp4s = "com.github.ghostdogpr" %% "caliban-http4s" % calibanVersion
  val calibanCats = "com.github.ghostdogpr" %% "caliban-cats" % calibanVersion

  val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"

  val discovery = "com.typesafe.akka" %% "akka-discovery" % "2.6.4"
  val discoveryLagom = "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.6.1"
  val discoveryKubernetes = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.5"

  val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.12.3"
  val logstage = "io.7mind.izumi" %% "logstage-core" % "0.10.8"
  val chimney = "io.scalaland" %% "chimney" % "0.5.0"

  val elastic4sVersion = "7.3.5"
  val elastic4s = "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion
  val esjava = "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion
  val esZio = "com.sksamuel.elastic4s" %% "elastic4s-effect-zio" % elastic4sVersion
  val esCirce = "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sVersion

  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.10"
  val scalaLogger = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
}
