import sbt._

import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object Eclipse {

  val settings = Seq (
    EclipseKeys.createSrc            := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
    EclipseKeys.projectFlavor        := EclipseProjectFlavor.Scala,
    EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18),
    EclipseKeys.withSource           := true,
    EclipseKeys.eclipseOutput        := Some("target/scala-2.11/classes")
  )
}
