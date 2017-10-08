package org.gerweck.scalafx.akka

import language.implicitConversions

import scala.collection.generic.{ Clearable, Growable }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

import akka.{ Done, NotUsed }
import akka.stream.Materializer
import akka.stream.scaladsl._

import scalafx.application.Platform.runLater
import scalafx.beans.Observable
import scalafx.beans.property._

import org.gerweck.scalafx.util.FutureObservable

trait AkkaFXCollections extends Any {
  implicit def sourceToRichSource[A, B](source: Source[A, B]) = new AkkaFXCollections.RichSource(source)
}

object AkkaFXCollections {
  def collectionWriterSink[A]
        (buffer: Observable with Growable[A] with Clearable,
         clearFirst: Boolean,
         groupingSize: Int = 50, groupingTimeout: FiniteDuration = 100.milliseconds)
      : Sink[A, Future[Done]] = {
    sealed trait PopulateAction
    case class InsertRows(data: Seq[A]) extends PopulateAction
    case object ClearData extends PopulateAction

    val grouping = {
      Flow[A]
        .groupedWithin(groupingSize, groupingTimeout)
        .map(InsertRows)
        .named("GroupInsertActions")
    }

    val clearData = {
      if (clearFirst)
        Source.single(ClearData)
      else
        Source.empty
    }.named("OptionalClearAction")

    val combinedSource: Flow[A, PopulateAction, NotUsed] = grouping.prepend(clearData)
    combinedSource .toMat {
      Sink .foreach[PopulateAction] {
        case ClearData        => runLater { buffer.clear() }
        case InsertRows(data) => runLater { buffer ++= data }
      } .named("BufferWriter")
    }(Keep.right)
  }

  class RichSource[A, B](val inner: Source[A, B]) extends AnyVal {
    def populateCollection[C >: A]
          (buffer: Observable with Growable[C] with Clearable,
           clearFirst: Boolean,
           groupingSize: Int = 50, groupingTimeout: FiniteDuration = 100.milliseconds)
          (implicit mat: Materializer, ec: ExecutionContext)
        : ReadOnlyObjectProperty[Option[Try[Done]]] = {

      val sink = collectionWriterSink(buffer, clearFirst, groupingSize, groupingTimeout)

      val graph = inner.toMat(sink)(Keep.right)

      FutureObservable.ofTryOption(graph.run())
    }
  }
}
