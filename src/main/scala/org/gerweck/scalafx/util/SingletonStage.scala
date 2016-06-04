package org.gerweck.scalafx.util

import scalafx.stage.Stage

import org.log4s._

/** A stage that should only be open at most once per application. */
abstract class SingletonStage {
  private[this] val logger = getLogger

  type InstanceStage <: ParentStage

  protected[this] var singletonStage: Option[InstanceStage] = None

  def name = getClass.getSimpleName

  protected[this] def makeStage(): InstanceStage

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

  protected trait ParentStage extends Stage {
    require(singletonStage.isEmpty, s"Cannot have two ${name} stages")
    logger.debug(s"Creating singleton ${name} stage")
  }
}
