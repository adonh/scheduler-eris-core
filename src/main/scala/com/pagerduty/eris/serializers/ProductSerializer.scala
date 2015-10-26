/*
 * Copyright (c) 2015, PagerDuty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.pagerduty.eris.serializers

import java.nio.ByteBuffer

import com.netflix.astyanax.Serializer
import com.netflix.astyanax.model.Composite
import com.netflix.astyanax.serializers.{AbstractSerializer, ComparatorType}

import scala.collection.JavaConversions._


/**
 * Allows to serialize tuples. Do not extend this class directly, use InferredSerializer instead:
 * {{{
 * import com.pagerduty.eris.serializers.implicits._
 * class MyTupleSerializer extends InferredSerializer[(Int, (String, Time))]
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
