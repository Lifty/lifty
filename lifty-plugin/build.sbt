sbtPlugin := true

version := "1.7.5-SNAPSHOT"

name := "lifty"

organization := "org.lifty"

scalaVersion := "2.9.1"

resolvers += "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

libraryDependencies += "org.scalatest" % "scalatest" % "1.3" % "test"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.4-M5"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.3"

libraryDependencies += "jline" % "jline" % "0.9.94"

publishTo <<= (version) { version: String =>
   val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
   val (name, url) = if (version.contains("-SNAPSHOT"))
                       ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
                     else
                       ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}

publishMavenStyle := false

scalacOptions ++= Seq("-Ydependent-method-types")