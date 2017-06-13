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

package com.pagerduty.eris

import com.netflix.astyanax.{Serializer => S, serializers => a}
import java.util.{Date, UUID}
import scala.collection.mutable

/**
  * We chose to omit Char and Short serializers. Because they are stored and compared as signed
  * Bytes, you will get unpredictable results when doing range queries. It is recommended to use
  * Integers in your model instead.
  */
package object serializers {

  // Type aliases, allow to reference serializers via classOf[T].
  type ByteArraySerializer = a.BytesArraySerializer
  type StringSerializer = a.StringSerializer
  type UuidSerializer = a.UUIDSerializer
  type DateSerializer = a.DateSerializer
  type BooleanSerializer = a.BooleanSerializer
  type ByteSerializer = a.ByteSerializer
  type DoubleSerializer = a.DoubleSerializer
  type FloatSerializer = a.FloatSerializer
  type IntSerializer = a.Int32Serializer
  type LongSerializer = a.LongSerializer

  /**
    * Allows to create serializers using type signatures. For example: serializer[(String, String)].
    */
  def serializer[T](implicit s: S[T]): S[T] = s

  // Direct references.
  implicit val ByteArraySerializer: S[Array[Byte]] = a.BytesArraySerializer.get
  implicit val StringSerializer: S[String] = a.StringSerializer.get
  implicit val UuidSerializer: S[UUID] = a.UUIDSerializer.get
  implicit val DateSerializer: S[Date] = a.DateSerializer.get

  // References with coerced types.
  implicit val BooleanSerializer: S[Boolean] = a.BooleanSerializer.get.asInstanceOf[S[Boolean]]
  implicit val ByteSerializer: S[Byte] = a.ByteSerializer.get.asInstanceOf[S[Byte]]
  implicit val DoubleSerializer: S[Double] = a.DoubleSerializer.get.asInstanceOf[S[Double]]
  implicit val FloatSerializer: S[Float] = a.FloatSerializer.get.asInstanceOf[S[Float]]
  implicit val IntSerializer: S[Int] = a.Int32Serializer.get.asInstanceOf[S[Int]]
  implicit val LongSerializer: S[Long] = a.LongSerializer.get.asInstanceOf[S[Long]]

  implicit object BigDecimalSerializer extends BigDecimalSerializer
  implicit object BigIntSerializer extends BigIntSerializer
  implicit object TimeUuidSerializer extends TimeUuidSerializer

  /**
    * A map of common serializers. Can be used with EntityMapper.
    */
  val CommonSerializers: Map[Class[_], S[_]] = {
    Map(
      classOf[Array[Byte]] -> ByteArraySerializer,
      classOf[String] -> StringSerializer,
      classOf[UUID] -> UuidSerializer,
      classOf[Date] -> a.DateSerializer.get,
      classOf[Boolean] -> BooleanSerializer,
      classOf[java.lang.Boolean] -> BooleanSerializer,
      classOf[Byte] -> ByteSerializer,
      classOf[java.lang.Byte] -> ByteSerializer,
      classOf[Double] -> DoubleSerializer,
      classOf[java.lang.Double] -> DoubleSerializer,
      classOf[Float] -> FloatSerializer,
      classOf[java.lang.Float] -> FloatSerializer,
      classOf[Int] -> IntSerializer,
      classOf[java.lang.Integer] -> IntSerializer,
      classOf[Long] -> LongSerializer,
      classOf[java.lang.Long] -> LongSerializer,
      classOf[BigDecimal] -> BigDecimalSerializer,
      classOf[BigInt] -> BigIntSerializer,
      classOf[TimeUuid] -> TimeUuidSerializer
    )
  }

  implicit def iBooleanObjSerializer: S[java.lang.Boolean] = a.BooleanSerializer.get
  implicit def iByteObjSerializer: S[java.lang.Byte] = a.ByteSerializer.get
  implicit def iDoubleObjSerializer: S[java.lang.Double] = a.DoubleSerializer.get
  implicit def iFloatObjSerializer: S[java.lang.Float] = a.FloatSerializer.get
  implicit def iIntObjSerializer: S[java.lang.Integer] = a.IntegerSerializer.get
  implicit def iLongObjSerializer: S[java.lang.Long] = a.LongSerializer.get

  implicit def iTuple2Serializer[T1, T2](implicit s1: S[T1], s2: S[T2]): S[(T1, T2)] = new ProductSerializer(
    Array(s1, s2).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1))
  )

  implicit def iTuple3Serializer[T1, T2, T3](implicit s1: S[T1], s2: S[T2], s3: S[T3]): S[(T1, T2, T3)] =
    new ProductSerializer(
      Array(s1, s2, s3).asInstanceOf[Array[S[Any]]],
      a => (a(0), a(1), a(2))
    )

  implicit def iTuple4Serializer[T1, T2, T3, T4](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4]
    ): S[(T1, T2, T3, T4)] = new ProductSerializer(
    Array(s1, s2, s3, s4).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3))
  )

  implicit def iTuple5Serializer[T1, T2, T3, T4, T5](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5]
    ): S[(T1, T2, T3, T4, T5)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4))
  )

  implicit def iTuple6Serializer[T1, T2, T3, T4, T5, T6](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6]
    ): S[(T1, T2, T3, T4, T5, T6)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5))
  )

  implicit def iTuple7Serializer[T1, T2, T3, T4, T5, T6, T7](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7]
    ): S[(T1, T2, T3, T4, T5, T6, T7)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6))
  )

  implicit def iTuple8Serializer[T1, T2, T3, T4, T5, T6, T7, T8](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7],
      s8: S[T8]
    ): S[(T1, T2, T3, T4, T5, T6, T7, T8)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7, s8).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7))
  )

  implicit def iTuple9Serializer[T1, T2, T3, T4, T5, T6, T7, T8, T9](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7],
      s8: S[T8],
      s9: S[T9]
    ): S[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7, s8, s9).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8))
  )

  implicit def iTuple10Serializer[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7],
      s8: S[T8],
      s9: S[T9],
      s10: S[T10]
    ): S[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9))
  )

  implicit def iTuple11Serializer[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7],
      s8: S[T8],
      s9: S[T9],
      s10: S[T10],
      s11: S[T11]
    ): S[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10))
  )

  implicit def iTuple12Serializer[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
      implicit s1: S[T1],
      s2: S[T2],
      s3: S[T3],
      s4: S[T4],
      s5: S[T5],
      s6: S[T6],
      s7: S[T7],
      s8: S[T8],
      s9: S[T9],
      s10: S[T10],
      s11: S[T11],
      s12: S[T12]
    ): S[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] = new ProductSerializer(
    Array(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12).asInstanceOf[Array[S[Any]]],
    a => (a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10), a(11))
  )
}

package serializers {

  /**
    * BigDecimalSerializers is an adapter for scala.BigDecimal type.
    */
  sealed class BigDecimalSerializer
      extends ProxySerializer[BigDecimal, java.math.BigDecimal](
        toRepresentation = _.bigDecimal,
        fromRepresentation = new BigDecimal(_),
        serializer = a.BigDecimalSerializer.get
      )

  /**
    * BigIntSerializer is an adapter for scala.BigInt type.
    */
  sealed class BigIntSerializer
      extends ProxySerializer[BigInt, java.math.BigInteger](
        toRepresentation = _.bigInteger,
        fromRepresentation = new BigInt(_),
        serializer = a.BigIntegerSerializer.get
      )

  /**
    * TimeUuidSerializer is an adapter for eris.TimeUuid type.
    */
  sealed class TimeUuidSerializer
      extends ProxySerializer[TimeUuid, java.util.UUID](
        toRepresentation = _.value,
        fromRepresentation = TimeUuid(_),
        serializer = a.TimeUUIDSerializer.get
      )
}
