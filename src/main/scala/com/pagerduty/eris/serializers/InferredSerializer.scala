package com.pagerduty.eris.serializers

import java.nio.ByteBuffer

import com.netflix.astyanax.Serializer
import com.netflix.astyanax.serializers.{AbstractSerializer, ComparatorType}


/**
 * Generic proxy serializer that infers the underlying serializer based on type signature.
 *
 * Example:
 * {{{
 * import com.pagerduty.eris.serializers.implicits._
 * class MySerializer extends InferredSerializer[(Int, (String, Time))]
 * }}}
 */
class InferredSerializer[T](implicit protected val serializer: Serializer[T])
  extends AbstractSerializer[T] with ValidatorClass
{
  def toByteBuffer(obj: T): ByteBuffer = serializer.toByteBuffer(obj)
  def fromByteBuffer(bytes: ByteBuffer): T = serializer.fromByteBuffer(bytes)
  override def getComparatorType(): ComparatorType = serializer.getComparatorType
  override def fromString(string: String): ByteBuffer = serializer.fromString(string)
  override def getString(bytes: ByteBuffer): String = serializer.getString(bytes)
  val validatorClass: String = ValidatorClass(serializer)
}
