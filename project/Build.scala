import sbt._
import Keys._

import com.typesafe.sbt.SbtSite.site
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport._

import scala.util.Properties.envOrNone
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

import Helpers._

sealed trait Basics {
  final val buildOrganization     = "org.gerweck.scalafx"
  final val buildOrganizationName = "Sarah Gerweck"
  final val buildOrganizationUrl  = Some("https://github.com/sarahgerweck")

  final val buildScalaVersion     = "2.11.7"
  final val extraScalaVersions    = Seq.empty
  final val minimumJavaVersion    = "1.8"
  lazy  val defaultOptimize       = true
  final val projectMainClass      = None

  lazy  val parallelBuild         = false
  lazy  val cachedResolution      = true

  /* Metadata definitions */
  lazy val buildMetadata = Vector(
    licenses    := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage    := Some(url("https://github.com/sarahgerweck/scalafx-utils")),
    description := "ScalaFX Utilities",
    startYear   := Some(2015),
    scmInfo     := Some(ScmInfo(url("https://github.com/sarahgerweck/scalafx-utils"), "scm:git:git@github.com:sarahgerweck/scalafx-utils.git"))
  )

  lazy val developerInfo = {
    <developers>
      <developer>
        <id>sarah</id>
        <name>Sarah Gerweck</name>
        <email>sarah.a180@gmail.com</email>
        <url>https://github.com/sarahgerweck</url>
        <timezone>America/Los_Angeles</timezone>
      </developer>
    </developers>
  }
}

object BuildSettings extends Basics {
  /* Overridable flags */
  lazy val optimize     = boolFlag("OPTIMIZE") orElse boolFlag("OPTIMISE") getOrElse defaultOptimize
  lazy val deprecation  = boolFlag("NO_DEPRECATION") map (!_) getOrElse true
  lazy val inlineWarn   = boolFlag("INLINE_WARNINGS") getOrElse false
  lazy val debug        = boolFlag("DEBUGGER") getOrElse false
  lazy val debugPort    = envOrNone("DEBUGGER_PORT") map { _.toInt } getOrElse 5050
  lazy val debugSuspend = boolFlag("DEBUGGER_SUSPEND") getOrElse true
  lazy val unusedWarn   = boolFlag("UNUSED_WARNINGS") getOrElse false
  lazy val importWarn   = boolFlag("IMPORT_WARNINGS") getOrElse false

  lazy val buildScalaVersions = buildScalaVersion +: extraScalaVersions
  val buildScalacOptions = Seq (
    "-unchecked",
    "-feature",
    "-target:jvm-" + minimumJavaVersion
  ) ++ (
    if (deprecation) Seq("-deprecation") else Seq.empty
  ) ++ (
    if (optimize) Seq("-optimize") else Seq.empty
  ) ++ (
    if (inlineWarn) Seq("-Yinline-warnings") else Seq.empty
  ) ++ (
    if (unusedWarn) Seq("-Ywarn-unused") else Seq.empty
  ) ++ (
    if (importWarn) Seq("-Ywarn-unused-import") else Seq.empty
  )

  /* Java build setup */
  val buildJavacOptions = Seq(
    "-target", minimumJavaVersion,
    "-source", minimumJavaVersion
  ) ++ (
    if (deprecation) Seq("-Xlint:deprecation") else Seq.empty
  )

  /* Site setup */
  lazy val siteSettings = site.settings ++ site.includeScaladoc()

  val buildSettings = buildMetadata ++
                      siteSettings ++
                      projectMainClass.toSeq.map(mainClass := Some(_)) ++
                      Seq (
    organization         :=  buildOrganization,
    organizationName     :=  buildOrganizationName,
    organizationHomepage :=  buildOrganizationUrl map { url _ },

    scalaVersion         :=  buildScalaVersion,
    crossScalaVersions   :=  buildScalaVersions,

    scalacOptions        ++= buildScalacOptions,
    javacOptions         ++= buildJavacOptions,
    autoAPIMappings      :=  true,

    updateOptions        :=  updateOptions.value.withCachedResolution(cachedResolution),
    parallelExecution    :=  parallelBuild,

    evictionWarningOptions in update :=
      EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false)
  )
}

object Helpers {
  def getProp(name: String): Option[String] = sys.props.get(name) orElse sys.env.get(name)
  def parseBool(str: String): Boolean = Set("yes", "y", "true", "t", "1") contains str.trim.toLowerCase
  def boolFlag(name: String): Option[Boolean] = getProp(name) map { parseBool _ }
  def boolFlag(name: String, default: Boolean): Boolean = boolFlag(name) getOrElse default
  def opts(names: String*): Option[String] = names.view.map(getProp _).foldLeft(None: Option[String]) { _ orElse _ }

  import scala.xml._
  def excludePomDeps(exclude: (String, String) => Boolean): Node => Node = { node: Node =>
    val rewriteRule = new transform.RewriteRule {
      override def transform(n: Node): NodeSeq = {
        if ((n.label == "dependency") && exclude((n \ "groupId").text, (n \ "artifactId").text))
          NodeSeq.Empty
        else
          n
      }
    }
    val transformer = new transform.RuleTransformer(rewriteRule)
    transformer.transform(node)(0)
  }
}

object Resolvers {
  val sonatypeSnaps   = Resolver.sonatypeRepo("snapshots")
  val sonatypeRelease = Resolver.sonatypeRepo("releases")
  val sonatypeStaging = "Sonatype Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

object Publish {
  import BuildSettings._
  import Resolvers._
  import Helpers._

  val sonaCreds = (
    for {
      user <- getProp("SONATYPE_USER")
      pass <- getProp("SONATYPE_PASS")
    } yield {
      credentials +=
          Credentials("Sonatype Nexus Repository Manager",
                      "oss.sonatype.org",
                      user, pass)
    }
  ).toSeq

  val settings = sonaCreds ++ Seq (
    publishMavenStyle       := true,
    pomIncludeRepository    := { _ => false },
    publishArtifact in Test := false,

    publishTo               := {
      if (version.value.trim endsWith "SNAPSHOT")
        Some(sonatypeSnaps)
      else
        Some(sonatypeStaging)
    },

    pomExtra                := developerInfo
  )

  /** Use this if you don't want to publish a certain module.
    * (SBT's release plugin doesn't handle this well.)
    */
  val falseSettings = settings ++ Seq (
    publishArtifact in Compile := false,
    publishArtifact in Test := false,
    publishTo := Some(Resolver.file("phony-repo", file("target/repo")))
  )
}

object Release {
  val settings = Seq (
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}

object Eclipse {
  import com.typesafe.sbteclipse.plugin.EclipsePlugin._

  val settings = Seq (
    EclipseKeys.createSrc            := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.projectFlavor        := EclipseProjectFlavor.Scala,
    EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18),
    EclipseKeys.withSource           := true,
    EclipseKeys.eclipseOutput        := Some("target/scala-2.11/classes")
  )
}

object Dependencies {
  /* ********************************************************************** */
  /*                          Utility Dependencies                          */
  /* ********************************************************************** */
  final val slf4jVersion       = "1.7.12"
  final val log4sVersion       = "1.2.0"
  final val logbackVersion     = "1.1.3"
  final val threeTenVersion    = "1.3"
  final val commonsVfsVersion  = "2.0"
  final val commonsIoVersion   = "2.4"
  final val spireVersion       = "0.9.1"
  final val groovyVersion      = "2.4.4"
  final val scalaParserVersion = "1.0.4"
  final val scalaXmlVersion    = "1.0.5"
  final val gerweckUtilVersion = "1.5.0"
  final val scalazVersion      = "7.1.3"
  final val shapelessVersion   = "2.2.5"
  final val scallopVersion     = "0.9.5"

  val log4s       = "org.log4s"           %% "log4s"               % log4sVersion
  val slf4j       = "org.slf4j"           %  "slf4j-api"           % slf4jVersion
  val jclBridge   = "org.slf4j"           %  "jcl-over-slf4j"      % slf4jVersion
  val log4jBridge = "org.slf4j"           %  "log4j-over-slf4j"    % slf4jVersion
  val logback     = "ch.qos.logback"      %  "logback-classic"     % logbackVersion
  val threeTen    = "org.threeten"        %  "threetenbp"          % threeTenVersion
  val spire       = "org.spire-math"      %% "spire"               % spireVersion
  val commonsIo   = "commons-io"          %  "commons-io"          % commonsIoVersion
  val groovy      = "org.codehaus.groovy" %  "groovy-all"          % groovyVersion
  val gerweckUtil = "org.gerweck.scala"   %% "gerweck-utils-java8" % gerweckUtilVersion
  val scalaz      = "org.scalaz"          %% "scalaz-core"         % scalazVersion
  val shapeless   = "com.chuusai"         %% "shapeless"           % shapelessVersion
  val scallop     = "org.rogach"          %% "scallop"             % scallopVersion

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
  final val scalaFxVersion = "8.0.40-R8"

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

object UtilsBuild extends Build {
  build =>

  import BuildSettings._
  import Resolvers._
  import Dependencies._
  import Helpers._

  lazy val root = (project in file ("."))
    .settings(buildSettings: _*)
    .settings(Eclipse.settings: _*)
    .settings(Publish.settings: _*)
    .settings(Release.settings: _*)
    .settings(resolvers += sonatypeRelease)
    .settings(
      name := "ScalaFX Utils",

      libraryDependencies ++= Seq (
        log4s,
        slf4j,
        jclBridge   % "runtime,optional",
        log4jBridge % "runtime,optional",
        logback     % "runtime,optional",
        gerweckUtil,
        scalaFx,
        scalaz,
        shapeless
      ),

      unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))
    )
}
