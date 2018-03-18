package org.gerweck.scalafx.util

import scalafx.beans.property._
import scalafx.beans.value.ObservableValue

sealed trait ObservableTag[A] {
  type JavaType
  type ObservableType = ObservableValue[A, JavaType]
  type ROPropType <: ReadOnlyProperty[A, JavaType] with ObservableType
  type PropType <: Property[A, JavaType] with ROPropType

  def property(startValue: A): PropType
}

object ObservableTag extends ObservableTagLowPriorityImplicits {
  type Aux[A, B] = ObservableTag[A] { type JavaType = B }

  implicit object IntTag extends ObservableTag[Int] {
    override type JavaType = java.lang.Number
    override type PropType = IntegerProperty
    override type ROPropType = ReadOnlyIntegerProperty
    override def property(i: Int): PropType = IntegerProperty(i)
  }

  implicit object LongTag extends ObservableTag[Long] {
    override type JavaType = java.lang.Number
    override type PropType = LongProperty
    override type ROPropType = ReadOnlyLongProperty
    override def property(l: Long): PropType = LongProperty(l)
  }

  implicit object FloatTag extends ObservableTag[Float] {
    override type JavaType = java.lang.Number
    override type PropType = FloatProperty
    override type ROPropType = ReadOnlyFloatProperty
    override def property(f: Float): PropType = FloatProperty(f)
  }

  implicit object DoubleTag extends ObservableTag[Double] {
    override type JavaType = java.lang.Number
    override type PropType = DoubleProperty
    override type ROPropType = ReadOnlyDoubleProperty
    override def property(d: Double): PropType = DoubleProperty(d)
  }

  implicit object BooleanTag extends ObservableTag[Boolean] {
    override type JavaType = java.lang.Boolean
    override type PropType = BooleanProperty
    override type ROPropType = ReadOnlyBooleanProperty
    override def property(b: Boolean): PropType = BooleanProperty(b)
  }

  implicit object StringTag extends ObservableTag[String] {
    override type JavaType = java.lang.String
    override type PropType = StringProperty
    override type ROPropType = ReadOnlyStringProperty
    override def property(s: String): PropType = StringProperty(s)
  }
}

sealed trait ObservableTagLowPriorityImplicits {
  final class ObjectTag[A] private[ObservableTagLowPriorityImplicits]() extends ObservableTag[A] {
    override type JavaType = A
    override type PropType = ObjectProperty[A]
    override type ROPropType = ReadOnlyObjectProperty[A]
    override def property(a: A): PropType = ObjectProperty[A](a)
  }
  implicit def anyTag[A]: ObjectTag[A] = new ObjectTag[A]
}
