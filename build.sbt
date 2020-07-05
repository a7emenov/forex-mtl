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
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

val forex = (project in file("."))
  .settings(
    name := "forex",
    version := "1.0.1",
    scalaVersion := "2.12.10",
    addCompilerPlugin(Libraries.kindProjector)
  ).settings(
    // Project dependencies
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
  ).settings(
    // Test dependencies
    libraryDependencies ++= Seq(
      Libraries.scalaTest,
      Libraries.scalaCheck,
      Libraries.catsScalaCheck
    ).map(_ % Test)
  )
