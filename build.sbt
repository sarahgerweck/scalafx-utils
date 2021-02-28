import Dependencies._
import Helpers._
import Resolvers._

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

lazy val root = (project in file ("."))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
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
      scalaFx,
      cats,
      shapeless,
      scalaCollectionCompat
    ),

    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % "15.0.1" classifier osName
    ),
  )
