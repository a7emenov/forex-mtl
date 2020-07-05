import Dependencies._
import sbt.Keys.version

ThisBuild / resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Ydelambdafy:method",
  "-Xlog-reflective-calls",
  "-Ywarn-macros:after",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

val forex = (project in file("."))
  // General settings
  .settings(
    name := "forex-mtl",
    version := "1.1.0",
    scalaVersion := "2.12.10",
  )
  // Docker build settings
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .settings(
    dockerBaseImage := "openjdk:8-alpine",
    dockerExposedPorts := Seq(8080),
    dockerEnvVars := Map("API_PORT" -> "8080"),
    dockerUpdateLatest := true
  )
  // Project dependencies
  .settings(
    addCompilerPlugin(Libraries.kindProjector),
    libraryDependencies ++= Seq(
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.fs2,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sCirce,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeGenericExt,
      Libraries.circeParser,
      Libraries.circeJava8,
      Libraries.pureConfig,
      Libraries.enumeratum,
      Libraries.slf4j,
      Libraries.airframeLog
    )
  )
  // Test dependencies
  .settings(
    libraryDependencies ++= Seq(
      Libraries.scalaTest,
      Libraries.scalaTestPlus,
      Libraries.scalaCheck
    ).map(_ % Test)
  )
