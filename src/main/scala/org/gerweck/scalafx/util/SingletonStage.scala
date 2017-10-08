package org.gerweck.scalafx.util

import scalafx.beans.property.{ ObjectProperty, ReadOnlyObjectProperty }
import scalafx.stage.Stage

import org.log4s._

/** A stage that should only be open at most once per application.
  *
  * To use this, you should do something like this:
  * {{{
  * class AboutWindow extends SingletonStage {
  *   type InstanceStage = AboutStage
  *   protected[this] def makeStage() = new Stage with AboutStage
  *   trait AboutStage extends super.ParentStage { stage =>
  *     title = "About My Application"
  *     scene = new Scene {
  *       ???
  *     }
  *   }
  * }
  * }}}
  */
abstract class SingletonStage {
  private[this] val logger = getLogger

  /** The specific type of the underlying stage that you'll create. */
  type InstanceStage <: ParentStage

  private[this] object State {
    private[this] val stageProp = ObjectProperty(Option.empty[InstanceStage])
    def currentStage = stageProp.value
    def currentStage_=(stage: Option[InstanceStage]): Unit = {
      stageProp.value = stage
    }
    def stage = stageProp.readOnly
  }

  /** Get the current stage (or `None` if it doesn't exist). */
  def stage: ReadOnlyObjectProperty[Option[InstanceStage]] = State.stage

  protected[this] def singletonStageName = getClass.getSimpleName

  protected[this] def makeStage(): InstanceStage

  /** Create the stage, or give it focus if it already exists.
    *
    * This needs to be executed within the UI thread. If you're not already within some kind
    * of event handler, you need to use [[scalafx.application.Platform.runLater]].
    */
  def showStage(): Unit = {
    State.currentStage match {
      case Some(stg) =>
        logger.debug("Singleton ${name} stage is already open")
        stg.requestFocus()
      case None =>
        val ns = makeStage()
        State.currentStage = Some(ns)
        ns.show()
    }
  }

  protected[this] trait ParentStage extends Stage {
    require(State.currentStage.isEmpty, s"Cannot have two ${singletonStageName} stages")
    logger.trace(s"Creating singleton ${singletonStageName} stage")

    /** Override this if you need to provide custom `close` behavior. */
    protected[this] def onClose(): Unit = ()

    override final def close() = {
      State.currentStage = None
      onClose()
      super.close()
    }
  }
}
