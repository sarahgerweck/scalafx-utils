package org.gerweck.scalafx.util

import scalafx.scene.input.Dragboard

import org.log4s._

/** Miscellaneous utility methods for debugging activity. */
object ScalaFXDebugTools {

  /** Print information about drag-and-drop content to the logs.
    *
    * This can get quite noisy if you do it on DragOver, so typically this
    * would only be used during debugging and disabled altogether in the real
    * product.
    */
  def logDragboardInfo(logger: Logger, level: LogLevel, showData: Boolean = true)(drb: Dragboard): Unit = {
    val summary = {
      val m =
        Map("html"  -> drb.hasHtml,
            "text"  -> drb.hasString,
            "url"   -> drb.hasUrl,
            "image" -> drb.hasImage,
            "rtf"   -> drb.hasRtf)
      m.map{case (k, v) => s"has${k.capitalize}: $v"}.mkString("; ")
    }
    var pieces: Seq[String] = Vector(summary)
    if (showData) {
      if (drb.hasUrl) {
        pieces :+= s"Dragboard URL: ${drb.url}"
      }
      if (drb.hasHtml) {
        pieces :+= s"Dragboard HTML: ${drb.html}"
      }
      if (drb.hasString) {
        pieces :+= s"Dragboard String: ${drb.string}"
      }
      if (drb.hasRtf) {
        pieces :+= s"Dragboard RTF: ${drb.rtf}"
      }
    }
    logger(level)(pieces.mkString("\n"))
  }
}
