/** Basic metadata about the project that gets pulled into the build */
trait ProjectSettings
    extends SettingTemplate
    with SettingTemplate.ApacheLicensed
    with SettingTemplate.GithubProject {
  override final val buildOrganization     = "org.gerweck.scalafx"
  override final val buildOrganizationName = "Sarah Gerweck"
  override final val projectDescription    = "Utilities to simplify ScalaFX applications"
  override final val projectStartYear      = 2015

  override final val githubOrganization    = "sarahgerweck"
  override final val githubProject         = "scalafx-utils"

  override final val buildScalaVersion     = "2.12.4"
  override final val extraScalaVersions    = Seq("2.11.12")
  override final val defaultOptimize       = true
  override final val defaultOptimizeGlobal = false
  override final val extraScalacOptions    = Seq("-Ypartial-unification")

  override final val sonatypeResolver      = true

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
