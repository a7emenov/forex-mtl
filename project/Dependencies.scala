import sbt._

object Dependencies {

  object Versions {
    val cats                = "2.1.0"
    val catsEffect          = "2.0.0"
    val fs2                 = "2.1.0"
    val http4s              = "0.20.15"
    val circe               = "0.11.1"
    val pureConfig          = "0.12.1"
    val enumeratum          = "1.6.1"

    val kindProjector       = "0.9.10"
    val slf4j               = "1.7.21"
    val airframeLog         = "20.6.2"
    val scalaTest           = "3.1.0"
    val scalaTestPlus       = "3.2.0.0"
    // Check scalatest-plus dependency before updating.
    val scalaCheck          = "1.14.3"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sClient        = http4s("http4s-blaze-client")
    lazy val http4sCirce         = http4s("http4s-circe")
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")
    lazy val circeJava8          = circe("circe-java8")
    lazy val pureConfig          = "com.github.pureconfig" %% "pureconfig"                 % Versions.pureConfig

    lazy val enumeratum          =  "com.beachape"         %% "enumeratum-cats"            % Versions.enumeratum

    // Compiler plugins
    lazy val kindProjector       = "org.spire-math"        %% "kind-projector"             % Versions.kindProjector

    // Logging
    // See https://wvlet.org/airframe/docs/airframe-log#using-with-slf4j for using airframe with slf4j.
    lazy val slf4j               =  "org.slf4j"            % "slf4j-jdk14"                 % Versions.slf4j
    lazy val airframeLog         =  "org.wvlet.airframe"   %% "airframe-log"               % Versions.airframeLog

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaTestPlus       = "org.scalatestplus"     %% "scalacheck-1-14"            % Versions.scalaTestPlus
    lazy val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % Versions.scalaCheck
  }

}
