// package org.gerweck.scalafx.akka

// import scala.concurrent._

// import akka.Done
// import akka.stream.OverflowStrategy
// import akka.stream.scaladsl._

// import scalafx.application.Platform.runLater
// import scalafx.beans.property.Property
// import scalafx.beans.value.ObservableValue

// /** A master object that exposes all the Akka-ScalaFX bridges.
//   *
//   * @author Sarah Gerweck <sarah.a180@gmail.com>
//   */
// object AkkaFX extends AkkaStreamFX with AkkaFXCollections

// trait AkkaStreamFX extends Any {
//   /** A [[akka.stream.scaladsl.Sink]] that sends all values to a
//     * [[scalafx.beans.property.Property]].
//     *
//     * Each event that's written into the `Sink` will trigger an update of the
//     * `Property` with the streamed value.
//     */
//   def observableSink[A](prop: Property[A, _]): Sink[A, Future[Done]] = {
//     Sink.foreach[A] { a =>
//       runLater {
//         prop.value = a
//       }
//     }
//   }

//   /** A [[akka.stream.scaladsl.Source]] that generates an event for each
//     * change of an [[scalafx.beans.value.ObservableValue]].
//     *
//     * This source adds an `onChange` handler to the given `ObservableValue`.
//     * Each time it observes a change, the new value is pushed from the
//     * `Source`. The change handler is registered as soon as the source is
//     * materialized into a graph. It should be safe to use a single source
//     * in several graphs, as each will register its own change listener upon
//     * materialization.
//     *
//     * @param prop The value to observe.
//     *
//     * @param queueSize The maximum number of values to queue while waiting for
//     * the downstream flow to consume more data.
//     *
//     * @param overflow What to do when the queue is full because the downstream
//     * flow cannot keep up. The default behavior is to block, slowing the UI's
//     * main thread until some events are consumed, freeing space in the queue.
//     */
//   def observableSource[A](prop: ObservableValue[_, A],
//                           queueSize: Int = 10,
//                           overflow: OverflowStrategy = OverflowStrategy.backpressure
//                         )(implicit ec: ExecutionContext) = {
//     val src = Source
//       .queue[A](queueSize, overflow)
//       .mapMaterializedValue { m =>
//         val sub = {
//           prop.onChange { (dta, oldV, newV) =>
//             m.offer(newV)
//           }
//         }
//         m.watchCompletion() foreach { c =>
//           runLater {
//             sub.cancel()
//           }
//         }
//         m
//       }
//     src
//   }
// }
