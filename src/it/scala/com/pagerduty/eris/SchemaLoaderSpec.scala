package com.pagerduty.eris

import com.netflix.astyanax.Keyspace
import com.pagerduty.eris.schema.{ReplicationStrategy, NetworkTopologyStrategy, SimpleStrategy, SchemaLoader}
import com.pagerduty.eris.serializers.ValidatorClass
import com.pagerduty.eris.serializers._
import org.scalatest.{ FreeSpec, Matchers }

import scala.collection.JavaConversions._

class SchemaLoaderSpec extends FreeSpec with Matchers {
  val tpePref = "org.apache.cassandra.db.marshal"

  "SchemaLoader should" - {
    "load simple schema correctly" in {
      val cluster = (new TestClusterCtx).cluster
      val keyspace1 = cluster.getKeyspace("SchemaLoaderSpec1")
      val cf1 = ColumnFamilyModel[Int, Long, BigInt](keyspace1, "columnFamily1")
      val keyspace2 = cluster.getKeyspace("SchemaLoaderSpec2")
      val cf2 = ColumnFamilyModel[String, Float, Double](keyspace2, "columnFamily2")

      val cdefs = Set(cf1, cf2).map(_.columnFamilyDef(cluster))
      val schemaLoader = new SchemaLoader(cluster, cdefs)
      schemaLoader.loadSchema()

      {
        val props = cluster.describeKeyspace(keyspace1.getKeyspaceName).getProperties.toMap
        val pref = "cf_defs.columnFamily1"
        props should contain(s"$pref.key_validation_class" -> s"$tpePref.Int32Type")
        props should contain(s"$pref.comparator_type" -> s"$tpePref.LongType")
        props should contain(s"$pref.default_validation_class" -> s"$tpePref.IntegerType")
      }

      {
        val props = cluster.describeKeyspace(keyspace2.getKeyspaceName).getProperties.toMap
        val pref = "cf_defs.columnFamily2"
        props should contain(s"$pref.key_validation_class" -> s"$tpePref.UTF8Type")
        props should contain(s"$pref.comparator_type" -> s"$tpePref.FloatType")
        props should contain(s"$pref.default_validation_class" -> s"$tpePref.DoubleType")
      }

      schemaLoader.dropSchema()
    }

    val keyspaceName = "SchemaLoaderSpec1"

    def loadSchema(
      replicationStrategies: Map[String, ReplicationStrategy] = Map.empty.withDefaultValue(SimpleStrategy(1))
    )(
      factory: Keyspace => ColumnFamilyModel[_, _, _]
    ): Map[String, String] = {
      val cluster = (new TestClusterCtx).cluster
      val keyspace = cluster.getKeyspace(keyspaceName)
      val cfModel = factory(keyspace)
      val cfDefs = Set(cfModel).map(_.columnFamilyDef(cluster))
      val schemaLoader = new SchemaLoader(cluster, cfDefs, replicationStrategies)

      schemaLoader.loadSchema()
      val props = cluster.describeKeyspace(keyspace.getKeyspaceName).getProperties.toMap
      schemaLoader.dropSchema()

      val pref = s"cf_defs.${cfModel.name}."
      props.map {
        case (k, v) if (k.startsWith(pref)) => k.drop(pref.size) -> v
        case (k, v) => k -> v
      }
    }

    "load simple replication strategy correctly" in {
      val props = loadSchema(Map(keyspaceName -> SimpleStrategy(3)))(
        ks => ColumnFamilyModel[String, String, String](ks, "columnFamily1")
      )
      props should contain("strategy_class" -> "org.apache.cassandra.locator.SimpleStrategy")
      props should contain("strategy_options.replication_factor" -> "3")
    }

    "load network topology replication strategy correctly" in {
      val props = loadSchema(Map(keyspaceName -> NetworkTopologyStrategy("DC1" -> 1, "DC2" -> 2)))(
        ks => ColumnFamilyModel[String, String, String](ks, "columnFamily1")
      )
      props should contain(
        "strategy_class" -> "org.apache.cassandra.locator.NetworkTopologyStrategy"
      )
      props should contain("strategy_options.DC1" -> "1")
      props should contain("strategy_options.DC2" -> "2")
    }

    "load overriden validators correctly" in {
      val props = loadSchema() { keyspace =>
        ColumnFamilyModel[String, String, String](keyspace, "columnFamily1",
          new ColumnFamilySettings(
            rowKeyValidatorOverride = Some(ValidatorClass[Int]),
            colNameValidatorOverride = Some(ValidatorClass[Long]),
            colValueValidatorOverride = Some(ValidatorClass[BigInt])
          ))
      }
      props should contain(s"key_validation_class" -> s"$tpePref.Int32Type")
      props should contain(s"comparator_type" -> s"$tpePref.LongType")
      props should contain(s"default_validation_class" -> s"$tpePref.IntegerType")
    }
  }
}
