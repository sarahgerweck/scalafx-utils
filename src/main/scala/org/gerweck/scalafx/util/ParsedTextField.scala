/*
 * Copyright AtScale, Inc. 2015. All Rights Reserved.
 *
 * No part of this project or any of its contents may be reproduced, copied,
 * modified or adapted, without the prior written consent of AtScale, Inc..
 */

package org.gerweck.scalafx.util

import scalafx.scene.control.TextField

import org.log4s._

/**
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class ParsedTextField[A, B <: Parseable[A]](default: A)(implicit val parser: B) {

  private val logger = getLogger("org.gerweck.scalafx.util.ParsedTextField")

  val field = new TextField
  val property: parser.Prop = parser.makeNew(default)

  field.text = parser.toString(default)

  field.text onChange {
    val s: String = field.text()
    try {
      property.value = parser.fromString(s)
    } catch {
      case nfe: IllegalArgumentException =>
        // This is pretty normal while you're entering text: no need to make it loud
        logger.trace(s"String doesn't parse successfully: $s")
      case re: RuntimeException =>
        logger.warn(re)(s"Error while parsing string: $s")
    }
  }
}
