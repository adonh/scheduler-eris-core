package com.pagerduty.eris.serializers

import java.nio.ByteBuffer

import com.netflix.astyanax.Serializer
import com.netflix.astyanax.serializers.{AbstractSerializer, ComparatorType}


/**
 * Generic serializer that delegate serialization of type T to serializer of type R using
 * conversions. Extend this when you want delegate all the work to an existing serializer.
 */
class ProxySerializer[T, R](
    protected val toRepresentation: T => R,
    protected val fromRepresentation: R => T,
    protected val serializer: Serializer[R])
  extends AbstractSerializer[T] with ValidatorClass
{
  def toByteBuffer(obj: T): ByteBuffer = serializer.toByteBuffer(toRepresentation(obj))
  def fromByteBuffer(bytes: ByteBuffer): T = fromRepresentation(serializer.fromByteBuffer(bytes))
  override def getComparatorType(): ComparatorType = serializer.getComparatorType
  override def fromString(string: String): ByteBuffer = serializer.fromString(string)
  override def getString(bytes: ByteBuffer): String = serializer.getString(bytes)
  val validatorClass: String = ValidatorClass(serializer)
}
