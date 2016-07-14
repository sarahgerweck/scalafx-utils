package org.gerweck.scalafx.util

import scalafx.util.StringConverter

/** A collection of premade string converters.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
object StringConverters {
  /** A converter for an `Option[String]` that treats empty strings (or those
    * with only whitespace) as `None`.
    */
  lazy val OptionStringConverter = StringConverter[Option[String]](s => if (s.trim.isEmpty) None else Some(s), _.getOrElse(""))
}
