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
  final val projectHomepage       = None

  final val buildScalaVersion     = "2.12.3"
  final val extraScalaVersions    = Seq("2.11.11")
  final val minimumJavaVersion    = "1.8"
  final val defaultOptimize       = true
  final val defaultOptimizeGlobal = false
  final val inlinePatterns        = Seq("!akka.**,!slick.**")
  final val autoAddCompileOptions = true

  final val parallelBuild         = true
  final val cachedResolution      = true
  final val sonatypeResolver      = true

  final val defaultNewBackend     = false

  /* Metadata definitions */
  lazy val githubPage = url(s"https://github.com/${githubOrganization}/${githubProject}")
  lazy val buildMetadata = Vector(
    licenses    := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage    := Some(projectHomepage.getOrElse(githubPage)),
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

      autoAPIMappings      :=  true,

      updateOptions        :=  updateOptions.value.withCachedResolution(cachedResolution),
      parallelExecution    :=  parallelBuild,

      evictionWarningOptions in update :=
        EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false)
    ) ++ (
      if (autoAddCompileOptions) {
        addScalacOptions() ++ addJavacOptions()
      } else {
        Seq.empty
      }
    ) ++ (
      if (sonatypeResolver) {
        /* Many OSS projects push here and then appear in Maven Central later */
        Seq(resolvers += Resolver.sonatypeRepo("releases"))
      } else {
        Seq.empty
      }
    )
  )

  /* Overridable flags */
  lazy val optimize       = boolFlag("OPTIMIZE") orElse boolFlag("OPTIMISE") getOrElse defaultOptimize
  lazy val optimizeGlobal = boolFlag("OPTIMIZE_GLOBAL") getOrElse defaultOptimizeGlobal
  lazy val optimizeWarn   = boolFlag("OPTIMIZE_WARNINGS") getOrElse false
  lazy val noFatalWarn    = boolFlag("NO_FATAL_WARNINGS") getOrElse false
  lazy val deprecation    = boolFlag("NO_DEPRECATION") map (!_) getOrElse true
  lazy val inlineWarn     = boolFlag("INLINE_WARNINGS") getOrElse false
  lazy val debug          = boolFlag("DEBUGGER") getOrElse false
  lazy val debugPort      = envOrNone("DEBUGGER_PORT") map { _.toInt } getOrElse 5050
  lazy val debugSuspend   = boolFlag("DEBUGGER_SUSPEND") getOrElse true
  lazy val unusedWarn     = boolFlag("UNUSED_WARNINGS") getOrElse false
  lazy val importWarn     = boolFlag("IMPORT_WARNINGS") getOrElse false
  lazy val java8Flag      = boolFlag("BUILD_JAVA_8") getOrElse false
  lazy val newBackend     = boolFlag("NEW_BCODE_BACKEND") getOrElse defaultNewBackend

  lazy val buildScalaVersions = buildScalaVersion +: extraScalaVersions

  def basicScalacOptions = Def.derive {
    scalacOptions ++= {
      var options = Seq.empty[String]
      val sv = sver.value

      options :+= "-unchecked"
      options :+= "-feature"
      if (deprecation) {
        options :+= "-deprecation"
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
      if (sv.backend == SupportsNewBackend && newBackend) {
        options :+= "-Ybackend:GenBCode"
      }

      options
    }
  }

  def optimizationScalacOptions(optim: Boolean = optimize) = Def.derive {
    scalacOptions ++= {
      var options = Seq.empty[String]
      val sv = sver.value
      val fos = forceOldInlineSyntax.value

      if (optim) {
        def doNewWarn(): Unit = {
          if (optimizeWarn) {
            options :+= "-opt-warnings:_"
          }
        }

        if (sv.backend == NewBackend && !fos) {
          options :+= "-opt:l:inline"

          val inlineFrom = {
            var patterns = Seq.empty[String]
            if (optimizeGlobal) {
              patterns :+= "**"
            } else {
              patterns :+= "<sources>"
            }
            patterns ++= inlinePatterns
            patterns
          }

          options :+= inlineFrom.mkString("-opt-inline-from:", ":", "")

          doNewWarn()
        } else if (sv.backend == NewBackend && fos || sv.backend == SupportsNewBackend && newBackend) {
          if (optimizeGlobal) {
            options :+= "-opt:l:classpath"
          } else {
            options :+= "-opt:l:project"
          }
          doNewWarn()
        } else {
          options :+= "-optimize"
          if (optimizeWarn) {
            options :+= "-Yinline-warnings"
          }
        }
      }

      options
    }
  }

  def addScalacOptions(optim: Boolean = optimize) = new Def.SettingList(Seq(
    basicScalacOptions,
    optimizationScalacOptions(optim)
  ))

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

  def basicSiteSettings = Def.derive {
    scalacOptions in (Compile,doc) ++= Seq(
      "-groups",
      "-implicits",
      "-diagrams",
      "-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath,
      "-doc-source-url", s"https://github.com/${githubOrganization}/${githubProject}/blob/master€{FILE_PATH}.scala"
    )
  }
}
