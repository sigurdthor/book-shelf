import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerCommands
import sbt._
import sbt.Keys.{libraryDependencies, organization, resolvers, scalaVersion, scalacOptions, sourceManaged, version}
import sbtprotoc.ProtocPlugin.autoImport.PB

object Settings {

  private val grpcVersion = "1.29.0"

  val commonSettings = Def.settings(
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    organization in ThisBuild := "org.sigurdthor",
    version in ThisBuild := "1.0-SNAPSHOT",
    scalaVersion in ThisBuild := "2.13.2",
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

  val grpcSettings = Def.settings(PB.targets in Compile := Seq(
    scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value,
  ),
  libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    "io.grpc" % "grpc-netty" % grpcVersion
  ))

  val dockerSettings = Def.settings(
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd(
        "RUN",
        "chmod -R u+rwx,g+rwx,o+rwx /opt/docker"
      ),
      Cmd("USER", "1001")
    )
  )
}
