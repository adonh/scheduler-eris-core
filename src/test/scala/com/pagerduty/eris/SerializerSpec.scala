package com.pagerduty.eris.core

import com.netflix.astyanax.Serializer
import com.pagerduty.eris.TimeUuid
import org.scalatest.{Matchers, FreeSpec}
import com.pagerduty.eris.serializers._


class SerializerSpec extends FreeSpec with Matchers {
  // Test product serialize via tuple implicits
  // Test inferred serializers

  def basicTestsForSerializers[T](
      validatorClass: String, serializer: Serializer[T], testValues: T*
    )(implicit implicitSerializer: Serializer[T])
  : Unit = {
    "have correct validatorClass" in {
      ValidatorClass(serializer) shouldBe validatorClass
    }
    "serialize and deserialize correctly" in {
      for (testValue <- testValues) {
        val bytes = serializer.toBytes(testValue)
        val deserialzied = serializer.fromBytes(bytes)
        deserialzied shouldBe testValue
      }
    }
  }

  def testDefaultSerializer[T](
      name: String, validatorClass: String,
      targetClasses: Seq[Class[_]], serializer: Serializer[T], testValues: T*
    )(implicit implicitSerializer: Serializer[T])
  : Unit = {
    s"$name serializer should" - {
      basicTestsForSerializers[T](validatorClass, serializer, testValues: _*)

      "provide implicit serializer" in {
        implicitSerializer.getClass shouldBe serializer.getClass
      }
      "be present in default mapping" in {
        targetClasses.map(CommonSerializers).forall(_.getClass == serializer.getClass)
      }
    }
  }

  def testStringTupleSerializer[T](
      n: Int, testValues: T*
    )(implicit implicitSerializer: Serializer[T])
  : Unit = {
    val name = (0 until n).map(_ => "String").mkString("(", ", ", ")")
    val validatorClass = (0 until n).map(
      _ => "org.apache.cassandra.db.marshal.UTF8Type"
    ).mkString("CompositeType(", ",", ")")

    s"$name serializer should" - {
      basicTestsForSerializers[T](validatorClass, serializer, testValues: _*)
    }
  }

  "Common serializers should" - {
    testDefaultSerializer("Array[Byte]", "org.apache.cassandra.db.marshal.BytesType",
      Seq(classOf[Array[Byte]]), ByteArraySerializer, "TestValue".getBytes)

    testDefaultSerializer("String", "org.apache.cassandra.db.marshal.UTF8Type",
      Seq(classOf[String]), StringSerializer, "TestValue")

    testDefaultSerializer("Uuid", "org.apache.cassandra.db.marshal.UUIDType",
      Seq(classOf[java.util.UUID]), UuidSerializer, TimeUuid().value, TimeUuid().value)

    testDefaultSerializer("Date", "org.apache.cassandra.db.marshal.DateType",
      Seq(classOf[java.util.Date]), DateSerializer, new java.util.Date())

    testDefaultSerializer("Boolean", "org.apache.cassandra.db.marshal.BooleanType",
      Seq(classOf[Boolean], classOf[java.lang.Boolean]), BooleanSerializer, true, false)

    testDefaultSerializer("Byte", "org.apache.cassandra.db.marshal.BytesType",
      Seq(classOf[Byte], classOf[java.lang.Byte]), ByteSerializer, -5.toByte, 0.toByte, 10.toByte)

    testDefaultSerializer("Double", "org.apache.cassandra.db.marshal.DoubleType",
      Seq(classOf[Double], classOf[java.lang.Double]), DoubleSerializer, -0.5, 0.0, 5.0)

    testDefaultSerializer("Float", "org.apache.cassandra.db.marshal.FloatType",
      Seq(classOf[Float], classOf[java.lang.Float]), FloatSerializer, -0.5f, 0.0f, 5.0f)

    testDefaultSerializer("Int", "org.apache.cassandra.db.marshal.Int32Type",
      Seq(classOf[Int], classOf[java.lang.Integer]), IntSerializer, -5, 0, 10)

    testDefaultSerializer("Long", "org.apache.cassandra.db.marshal.LongType",
      Seq(classOf[Long], classOf[java.lang.Long]), LongSerializer, -5L, 0L, 10L)
  }

  "Proxy serializers should" - {
    testDefaultSerializer("BigDecimal", "org.apache.cassandra.db.marshal.DecimalType",
      Seq(classOf[BigDecimal]), BigDecimalSerializer,
      BigDecimal(-0.5), BigDecimal(0.0), BigDecimal(5.0))

    testDefaultSerializer("BigInt", "org.apache.cassandra.db.marshal.IntegerType",
      Seq(classOf[BigInt]), BigIntSerializer,
      BigInt(-5), BigInt(0), BigInt(10))

    testDefaultSerializer("TimeUuid", "org.apache.cassandra.db.marshal.TimeUUIDType",
      Seq(classOf[TimeUuid]), TimeUuidSerializer, TimeUuid(), TimeUuid())
  }
  "Product serializers should" - {
    testStringTupleSerializer(2,
      ("v1", "v2"))

    testStringTupleSerializer(3,
      ("v1", "v2", "v3"))

    testStringTupleSerializer(4
      , ("v1", "v2", "v3", "v4"))

    testStringTupleSerializer(5,
      ("v1", "v2", "v3", "v4", "v5"))

    testStringTupleSerializer(6,
      ("v1", "v2", "v3", "v4", "v5", "v6"))

    testStringTupleSerializer(7,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7"))

    testStringTupleSerializer(8,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8"))

    testStringTupleSerializer(9,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"))

    testStringTupleSerializer(10,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10"))

    testStringTupleSerializer(11,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11"))

    testStringTupleSerializer(12,
      ("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11", "v12"))
  }

  "Inferred serializer should" - {
    class TupleIntStringSerializer extends InferredSerializer[(Int, String)]
    val t1 = "org.apache.cassandra.db.marshal.Int32Type"
    val t2 = "org.apache.cassandra.db.marshal.UTF8Type"
    basicTestsForSerializers(s"CompositeType($t1,$t2)",
      new TupleIntStringSerializer, (1, "v1"), (2, "v2"))
  }
}
