// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.28")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.2"
libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.2.0+11-81948f48-SNAPSHOT"