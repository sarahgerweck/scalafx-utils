# Sarah’s ScalaFX Utilities #

This project contains helper functions, utilities and convenience functions
for working with JavaFX and ScalaFX in Scala.

ScalaFX does a tremendous job at making JavaFX more usable from Scala, but
it doesn't go as far as it could in facilitating functional and reactive
programming. This project is an attempt to add additional facilities that
further bridge the beautiful Scala with JavaFX.

In particular, here are some key features:

   * Monadic and applicative interfaces on top of `Observable` make it easy to
     build up computations.
   * Converters that allow you to use a `Future` or Akka `Stream` as an
     `Observable`.

This code is offered as is with no guarantees. You are free to use it if you
find it useful, but this is not part of any production project and it may have
serious bugs. These APIs may also change at any time, and I make no guarantees
that the project will be maintained at all. I welcome any bug reports and I
will be happy to merge high-quality pull requests if you find a bug.

## Installation ##

To use ScalaFX, add the following to your SBT build:

    libraryDependencies += "org.gerweck.scala" %% "scalafx-utils" % "0.12.0"

## Usage ##

The primary use of this library is to provide a number of implicit conversions
and instances, which are all brought into scope with this import:

    import org.gerweck.scalafx.util._

If you use Scalaz, this makes ScalaFX observables instances of `Functor`,
`Applicative` and `Monad`. It also provides some simple extension methods
along these lines.

### Functional Transformations ###

Note that the output of a functional transformation is always a
`ReadOnlyObjectProperty[A]`, even if there exists a more specific result type
like `ReadOnlyIntegerProperty` that would work. (The types used by ScalaFX are
fairly complicated, and no real harm is done by using an `ObjectProperty` in
all cases.)

#### Map ####

To facilitate functional programming, the standard `map` function allows you
to transform an observable value using a pure function.

Note that, for performance reasons, these functionally defined observables do
not trigger an update if an input or output value is changed to one that is
identical as defined by `equals`.

    import scalafx.beans.value._
    import scalafx.scene.control._
    import org.gerweck.scalafx.util._

    val textBox = new TextField { /* ... */ }
    val boxText: ObservableValue[String, String] = textBox.text
    /* Construct a new observable derived from the underlying one using `map` */
    val characterCount: ReadOnlyObjectProperty[String] = textBox.text map (_.size)

#### Multiple Function Inputs ####

If your function depends on several observable values, you can use the
applicative behavior provided by the library. The Scalaz applicative
functionality is all available, but there is a more convenient mechanism for
the most common use case where you want to operate on a tuple.

    import scalafx.beans.property._
    import org.gerweck.scalafx.util._

    val startedDownloads = IntegerProperty(0)
    val finishedDownloads = IntegerProperty(0)
    val runningDownloads: ReadOnlyObjectProperty[Int] =
      (startedDownloads, finishedDownloads).observe map {
        case (st, fi) => st - fi
      }

This `observe` extension method is available on tuples of any arity and
efficiently processes updates from any of its dependent values.

#### Monadically Chained Observables ####

In addition to the behavior of an applicative functor, this library also
provides the ability to act like a monadic functor by providing `flatMap` and
`flatten`. *Where possible use the applicative syntax defined above rather
than a chain of `flatMap` applications: the applicative format performs much
better.*

Here is an example of a model where you might have a dialog box or window.
In this window, you could have a list selection where you choose from one of
many transformation types. Once you've selected a transformation type, it will
display a configuration panel that you can use to control the details of that
transformation.

    import scalafx.beans.property._
    import org.gerweck.scalafx.util._

    /** An object that has a config dialog that produces a function */
    trait ConfigurableIntFunction {
      val typeName: String
      val configPanel: scalafx.scene.layout.Pane
      val currentFunction: ReadOnlyObjectProperty[Int => Int]
    }
    val selectedFunctionType: ObjectProperty[ConfigurableIntFunction] = ???
    val selectedFunction = selectedFunctionType flatMap (_.currentFunction)
    val inputInt = IntegerProperty(0)
    val outputInt =
      (selectedFunction, inputInt).observe map {
        case (sf, ii) => sf(ii)
      }
