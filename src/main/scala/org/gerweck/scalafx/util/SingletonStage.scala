package org.gerweck.scalafx.util

import scalafx.stage.Stage

import org.log4s._

/** A stage that should only be open at most once per application. */
abstract class SingletonStage {
  private[this] val logger = getLogger

  type InstanceStage <: ParentStage

  protected[this] final var singletonStage: Option[InstanceStage] = None

  protected[this] def singletonStageName = getClass.getSimpleName

  protected[this] def makeStage(): Stage with InstanceStage

  def showStage(): Unit = {
    singletonStage match {
      case Some(stg) =>
        logger.debug("Singleton ${name} stage is already open")
        stg.requestFocus()
      case None =>
        val ns = makeStage()
        singletonStage = Some(ns)
        ns.show()
    }
  }

  protected[this] trait ParentStage extends Stage {
    require(singletonStage.isEmpty, s"Cannot have two ${singletonStageName} stages")
    logger.trace(s"Creating singleton ${singletonStageName} stage")

    override def close() = {
      singletonStage = None
      super.close()
    }
  }
}
