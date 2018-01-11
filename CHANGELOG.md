# ScalaFX-Utils changelog

For a full change history, see the Git history. Only changes likely to affect
end users will be listed here.

## 0.x

Pre-release development. This is reasonably stable, but APIs are subject to
change.

### 0.6.0

* Add new operations to flatten observable collections.
* Updates to several dependencies.
  * This includes Shapeless 2.3.0, which may introduce binary
    incompatibilities if you have compiled against a different version.

### 0.7.0

* Modified Scalaz instance to add `ReadOnlyObjectProperty` instances and
  improve specificity of the `ObservableValue[A, _]` instances.

### 0.8.0

* Update ScalaFX to 8.0.92-R10.
  * This makes Java 1.8.0_91 the minimum version.
* `FutureObservable` allows you to observe the results of a `Future`.
* New `AkkaFX` object gives bridges from Akka to ScalaFX
  * `observableSink` creates a Akka Streams sink that allows you to
    observe each value that comes through a stream.

### 0.9.0

* This release is not binary compatible with 0.8 or earlier.
* Add more controls for filtering items.

#### 0.9.1

* Add control panel builders: simple utility code for building control
  panels

### 0.10.0

* Breaking changes to `SingletonStage` for better naming and visibility.
* New `layout` package with `Anchored` pane for resizable stages.
* New `StringConverters` with standard converter types.

### 0.11.0

* Update to ScalaFX 8.0.102-R11.
  * This makes Java 1.8.0_101 the official minimum version.
* Reorganize the build to meet SBT 1.0 guidelines.

#### 0.11.1

* Update to Akka 2.4.11.

#### 0.11.2

* Update to Akka 2.4.14.
* Add support for Scala 2.12

### 0.12

* Update to Akka 2.5
* Migrate to SBT 1.0
* Update to ScalaFX 8.0.144-R14
  * This is tested against Java 8u144, though it may work with older versions.
* Improvements to `SingletonStage`
* Update Scala to 2.12.4

### 0.13

* New `ObservablePref`
  * This builds on the `Pref` in `scala-utils`, making it a bindable property.
  * `Pref` and `ObservablePref` are the way I recommend to store things like
    window sizes, column selections and other UI preferences.
  * Use a database like [H2](http://www.h2database.com/) if you have
    complicated application state that needs to persist.

#### 0.13.1

* Update to SBT 1.1
* Update Shapeless to 2.3.3
* Update Scalaz to 7.2.18
* Update Gerweck Utils to 2.7.2
