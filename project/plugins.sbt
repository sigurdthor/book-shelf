// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.4")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.34")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
addSbtPlugin("org.scalameta" %% "sbt-scalafmt" % "2.4.0")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.9"
libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.4.1"