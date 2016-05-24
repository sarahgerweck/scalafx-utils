package org.gerweck.scalafx.util

import com.sun.javafx.PlatformUtil

/** Utilities for identifying the Host OS of the application.
  *
  * @author Sarah Gerweck <sarah.a180@gmail.com>
  */
object HostOS {
  lazy val isMac = PlatformUtil.isMac()
  lazy val isUnix = PlatformUtil.isUnix()
  lazy val isWindows = PlatformUtil.isWindows()
}
