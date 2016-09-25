import sbt._
import Keys._

import scala.util.Properties.envOrNone

import com.typesafe.sbt.site._

import Helpers._

sealed trait Basics {
  final val buildOrganization     = "org.gerweck.scalafx"
  final val buildOrganizationName = "Sarah Gerweck"
  final val buildOrganizationUrl  = Some("https://github.com/sarahgerweck")

  final val buildScalaVersion     = "2.11.8"
  final val extraScalaVersions    = Seq.empty
  final val minimumJavaVersion    = "1.8"
  lazy  val defaultOptimize       = false
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

object BasicSettings extends AutoPlugin with Basics {
  override def requires = SiteScaladocPlugin

  override lazy val projectSettings = (
    buildMetadata ++
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
  )

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
}

