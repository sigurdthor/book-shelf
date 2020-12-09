import sbt.Keys.libraryDependencies
import Dependencies.{lagomScaladslAkkaDiscovery, _}
import Settings._

lazy val `book-shelf` = (project in file("."))
  .aggregate(`book-api`, `book-impl`, `recommendation-api`, `recommendation-impl`, `graphql-gateway`, lib)

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies ++= Seq(
      logstage,
      logback,
      scalaLogger,
      commonsLang,
      zio
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
      pureconfig,
      chimney,
      macwire,
      scalaTest,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(commonSettings ++ dockerSettings ++ grpcSettings)
  .dependsOn(`book-api`, lib)

lazy val `recommendation-api` = (project in file("recommendation-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      cats
    )
  )

lazy val `recommendation-impl` = (project in file("recommendation-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaClient,
      lagomScaladslTestKit,
      macwire,
      elastic4s,
      esjava,
      esZio,
      esCirce,
      pureconfig,
      scalaTest,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(commonSettings ++ dockerSettings ++ grpcSettings)
  .dependsOn(`recommendation-api`, `book-api`, lib)

lazy val `graphql-gateway` = (project in file("graphql-gateway"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      http4s,
      http4sCirce,
      http4sDsl,
      catz,
      chimney,
      pureconfig,
      circe,
      circeOptics,
      caliban,
      calibanHttp4s,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi
    )
  )
  .settings(commonSettings ++ grpcSettings)
  .dependsOn(lib)

//lagomServiceLocatorEnabled in ThisBuild := false