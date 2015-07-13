package com.pagerduty.eris.serializers

import java.nio.ByteBuffer

import com.netflix.astyanax.Serializer
import com.netflix.astyanax.serializers.{AbstractSerializer, ComparatorType}


/**
 * Generic serializer that delegate serialization of type T to serializer of type U using
 * conversions. Extend this when you want delegate all the work to an existing serializer.
 */
class ProxySerializer[T, U](
    protected val extract: T => U,
    protected val repackage: U => T,
    protected val serializer: Serializer[U])
  extends AbstractSerializer[T] with ValidatorClass
{
  def toByteBuffer(obj: T): ByteBuffer = serializer.toByteBuffer(extract(obj))
  def fromByteBuffer(bytes: ByteBuffer): T = repackage(serializer.fromByteBuffer(bytes))
  override def getComparatorType(): ComparatorType = serializer.getComparatorType
  override def fromString(string: String): ByteBuffer = serializer.fromString(string)
  override def getString(bytes: ByteBuffer): String = serializer.getString(bytes)
  val validatorClass: String = ValidatorClass(serializer)
}
