package com.pagerduty.eris.serializers

import java.nio.ByteBuffer

import com.netflix.astyanax.Serializer
import com.netflix.astyanax.model.Composite
import com.netflix.astyanax.serializers.{AbstractSerializer, ComparatorType}

import scala.collection.JavaConversions._


/**
 * Allows to serialize tuples. Do not extend this class directly, use ImplicitSerializer instead:
 * {{{
 * import com.pagerduty.eris.serializers.implicits._
 * class MyTupleSerializer extends ImplicitSerializer[(Int, (String, Time))]
 * }}}
 */
class ProductSerializer[P <: Product](
    protected val serializers: IndexedSeq[Serializer[Any]],
    protected val factory: IndexedSeq[Any] => Product)
  extends AbstractSerializer[P] with ValidatorClass
{
  private[this] val serializerInjector: java.util.List[Serializer[_]] = serializers

  protected def mkCompositeValidatorClass(validatorClasses: Seq[String]): String = {
    "CompositeType" + validatorClasses.mkString("(", ",", ")")
  }

  def toByteBuffer(product: P): ByteBuffer = {
    require(product.productArity == serializers.size, "Product arity does not match serializer.")
    val composite = new Composite()
    for (i <- 0 until serializers.size) {
      composite.setComponent(i, product.productElement(i), serializers(i))
    }
    composite.serialize()
  }

  def fromByteBuffer(bytes: ByteBuffer): P = {
    val composite = new Composite()
    composite.setSerializersByPosition(serializerInjector)
    composite.deserialize(bytes)
    val components = for (i <- 0 until serializers.size) yield composite.get(i)
    factory(components).asInstanceOf[P]
  }

  override def getComparatorType(): ComparatorType = ComparatorType.COMPOSITETYPE

  val validatorClass: String = {
    val validatorClasses = serializers.map(ValidatorClass(_))
    mkCompositeValidatorClass(validatorClasses)
  }
}
