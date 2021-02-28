import sbt._

object Dependencies {
  /* ********************************************************************** */
  /*                          Utility Dependencies                          */
  /* ********************************************************************** */
  final val slf4jVersion                 = "1.7.30"
  final val log4sVersion                 = "1.9.0"
  final val logbackVersion               = "1.2.3"
  final val commonsVfsVersion            = "2.2"
  final val commonsIoVersion             = "2.4"
  final val spireVersion                 = "0.13.0"
  final val scalaParserVersion           = "1.0.4"
  final val scalaXmlVersion              = "1.0.5"
  final val catsVersion                  = "2.4.2"
  final val shapelessVersion             = "2.3.3"
  final val scallopVersion               = "1.0.1"
  final val scalaCollectionCompatVersion = "2.4.2"

  val log4s                 = "org.log4s"              %% "log4s"                   % log4sVersion
  val slf4j                 = "org.slf4j"              %  "slf4j-api"               % slf4jVersion
  val jclBridge             = "org.slf4j"              %  "jcl-over-slf4j"          % slf4jVersion
  val log4jBridge           = "org.slf4j"              %  "log4j-over-slf4j"        % slf4jVersion
  val logback               = "ch.qos.logback"         %  "logback-classic"         % logbackVersion
  val spire                 = "org.spire-math"         %% "spire"                   % spireVersion
  val commonsIo             = "commons-io"             %  "commons-io"              % commonsIoVersion
  val cats                  = "org.typelevel"          %% "cats-core"               % catsVersion
  val shapeless             = "com.chuusai"            %% "shapeless"               % shapelessVersion
  val scallop               = "org.rogach"             %% "scallop"                 % scallopVersion
  val scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion

  val commonsVfs = {
    val base      = "org.apache.commons"  %  "commons-vfs2"     % commonsVfsVersion
    base.exclude("commons-logging",      "commons-logging")
        .exclude("org.apache.maven.scm", "maven-scm-provider-svnexe")
        .exclude("org.apache.maven.scm", "maven-scm-api")
    base
  }

  /* Use like this: libraryDependencies <++= (scalaBinaryVersion) (scalaParser) */
  def scalaParser(optional: Boolean): String => Seq[ModuleID] = { scalaBinaryVersion: String =>
    optionalize(optional) {
      scalaBinaryVersion match {
        case "2.11" => Seq("org.scala-lang.modules" %% "scala-parser-combinators" % scalaParserVersion % "optional")
        case _      => Seq.empty
      }
    }
  }

  def scalaXml(optional: Boolean)(scalaBinaryVersion: String): String => Seq[ModuleID] = { scalaBinaryVersion: String =>
    optionalize(optional) {
      scalaBinaryVersion match {
        case "2.11" => Seq("org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion % "optional")
        case _      => Seq.empty
      }
    }
  }

  /* ********************************************************************** */
  /*                          Testing Dependencies                          */
  /* ********************************************************************** */
  final val scalaCheckVersion  = "1.12.2"
  final val scalaTestVersion   = "2.2.4"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  val scalaTest  = "org.scalatest"  %% "scalatest"  % scalaTestVersion

  /* ********************************************************************** */
  /*                                ScalaFX                                 */
  /* ********************************************************************** */
  final val scalaFxVersion = "15.0.1-R21"

  val scalaFx = "org.scalafx" %% "scalafx" % scalaFxVersion

  /* ********************************************************************** */
  /*                                Helpers                                 */
  /* ********************************************************************** */
  private[this] def optionalize(optional: Boolean)(f: => Seq[ModuleID]): Seq[ModuleID] = {
    if (optional) {
      f map { _ % "optional" }
    } else {
      f
    }
  }

  private[this] def noCL(m: ModuleID) = (
    m exclude("commons-logging", "commons-logging")
      exclude("commons-logging", "commons-logging-api")
  )
}


