import sbt._
import Keys._

import scala.util.Properties.envOrNone

import com.typesafe.sbt.site._

import Helpers._

sealed trait Basics {
  final val buildOrganization     = "org.gerweck.scalafx"
  final val buildOrganizationName = "Sarah Gerweck"
  final val buildOrganizationUrl  = Some("https://github.com/sarahgerweck")
  final val githubOrganization    = "sarahgerweck"
  final val githubProject         = "scalafx-utils"
  final val projectDescription    = "ScalaFX Utilities"
  final val projectStartYear      = 2015

  final val buildScalaVersion     = "2.11.8"
  final val extraScalaVersions    = Seq("2.12.1")
  final val minimumJavaVersion    = "1.8"
  final val defaultOptimize       = false

  final val parallelBuild         = false
  final val cachedResolution      = true

  final val defaultNewBackend     = false

  /* Metadata definitions */
  lazy val githubPage = url(s"https://github.com/${githubOrganization}/${githubProject}")
  lazy val buildMetadata = Vector(
    licenses    := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage    := Some(githubPage),
    description := projectDescription,
    startYear   := Some(projectStartYear),
    scmInfo     := Some(ScmInfo(githubPage, s"scm:git:git@github.com:${githubOrganization}/${githubProject}.git"))
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
    Seq (
      organization         :=  buildOrganization,
      organizationName     :=  buildOrganizationName,
      organizationHomepage :=  buildOrganizationUrl map { url _ },

      scalaVersion         :=  buildScalaVersion,
      crossScalaVersions   :=  buildScalaVersions,

      addScalacOptions(),
      addJavacOptions(),
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
  lazy val java8Flag    = boolFlag("BUILD_JAVA_8") getOrElse false
  lazy val newBackend   = boolFlag("NEW_BCODE_BACKEND") getOrElse defaultNewBackend

  lazy val buildScalaVersions = buildScalaVersion +: extraScalaVersions

  def addScalacOptions(optim: Boolean = optimize) = Def.derive {
    scalacOptions ++= {
      val sv = SVer(scalaBinaryVersion.value)
      var options = Seq.empty[String]

      options :+= "-unchecked"
      options :+= "-feature"
      if (deprecation) {
        options :+= "-deprecation"
      }
      if (inlineWarn) {
        options :+= "-Yinline-warnings"
      }
      if (unusedWarn) {
        options :+= "-Ywarn-unused"
      }
      if (importWarn) {
        options :+= "-Ywarn-unused-import"
      }
      if (!sv.requireJava8) {
        options :+= "-target:jvm-" + minimumJavaVersion
      }
      if (optim) {
        if (sv.newOptimize || sv.supportsNewBackend && newBackend) {
          options :+= "-opt:l:project"
        } else if (!sv.requireJava8) {
          options :+= "-optimize"
        }
      }
      if (sv.supportsNewBackend && newBackend && !sv.requireJava8) {
        options :+= "-Ybackend:GenBCode"
      }

      options
    }
  }

  def addJavacOptions() = Def.derive {
    javacOptions ++= {
      val sv = SVer(scalaBinaryVersion.value)
      var options = Seq.empty[String]

      if (sv.requireJava8) {
        options ++= Seq[String](
          "-target", "1.8",
          "-source", "1.8"
        )
      } else {
        options ++= Seq[String](
          "-target", minimumJavaVersion,
          "-source", minimumJavaVersion
        )
      }

      options
    }
  }
}

