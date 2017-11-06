package org.gerweck.scalafx.util
package prefs

import java.util.prefs.Preferences

import scalafx.application.Platform.runLater
import scalafx.beans.property.ObjectProperty

import org.gerweck.scala.util.prefs._

/* TODO: take an implicit that will deteremine whether this is an `ObjectProperty` or what */
class ObservablePref[A] protected (path: String)(implicit handler: Pref.AccessHandler[A], prefs: Preferences)
    extends Pref[A](path) {

  lazy val observe: ObjectProperty[A] = {
    val initialValue: A = this()
    val property = ObjectProperty[A](initialValue)

    /* We build two bridges, one that listens for changes in the preferences system and pushes
     * them into the observable property, and another that listens for updates to the property and
     * pushes them to the preference system. Each bridge is gated so that it only activates if the
     * value has actually changed, which prevents the infinite looping that would otherwise occur
     * in a bidirectional bridge. */

    /* Preferences => Property bridge */
    prefs.addPreferenceChangeListener { pce =>
      if (pce.getKey == path) {
        runLater {
          val currentValue = this()
          if (property.value != currentValue) {
            property.value = currentValue
          }
        }
      }
    }
    /* Property => Preferences bridge */
    property.onChange { (_, _, newV) =>
      if (newV != this()) {
        this() = newV
      }
      ()
    }
    property
  }
}

object ObservablePref {
  def apply[A](path: String)(implicit handler: PrefHandler[A], prefs: Preferences) = {
    new ObservablePref(path)(new Pref.AccessHandler.Optional, prefs)
  }
  def apply[A](path: String, default: A)(implicit handler: PrefHandler[A], prefs: Preferences) = {
    new ObservablePref(path)(new Pref.AccessHandler.Defaulted(default), prefs)
  }
}
