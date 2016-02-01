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

import com.netflix.astyanax.{ Keyspace, Cluster }
import com.netflix.astyanax.ddl.ColumnFamilyDefinition
import com.pagerduty.eris.schema.{ NetworkTopologyStrategy, SimpleStrategy, ReplicationStrategy, SchemaLoader }
import com.pagerduty.eris.serializers._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ Matchers, FreeSpec }
import scala.collection.JavaConversions._

class SchemaLoaderSpec extends FreeSpec with Matchers with MockFactory {
  val tpePref = "org.apache.cassandra.db.marshal"

  "SchemaLoader should" - {
    "drop keyspace correctly" in {
      val cluster = mock[Cluster]
      val keyspaceName = "testKeyspace"
      (cluster.dropKeyspace _).expects(keyspaceName)
      SchemaLoader.dropKeyspace(cluster, keyspaceName)
    }

    "drop schema correctly" in {
      val cluster = mock[Cluster]
      val keyspaceNames = Set("SchemaLoaderSpec1", "SchemaLoaderSpec2")
      val cdefs = for (keyspaceName <- keyspaceNames) yield {
        (cluster.dropKeyspace _).expects(keyspaceName)
        val cdef = mock[ColumnFamilyDefinition]
        (cdef.getKeyspace _).expects().returns(keyspaceName)
        cdef
      }
      val schemaLoader = new SchemaLoader(cluster, cdefs)
      schemaLoader.dropSchema()
    }

    "load simple schema correctly" in {
      val cluster = TestClusterCtx.cluster
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
      val cluster = TestClusterCtx.cluster
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

    def colProps(columns: ColumnModel*): Map[String, (Boolean, String)] = {
      val cluster = TestClusterCtx.cluster
      val keyspace = cluster.getKeyspace(keyspaceName)
      val cfModel = ColumnFamilyModel[String, String, String](
        keyspace, "columnFamily1", columns = columns.toSet
      )
      val colDefList = cfModel.columnFamilyDef(cluster).getColumnDefinitionList
      val colProps = for (colDef <- colDefList) yield {
        colDef.getName -> (colDef.hasIndex, colDef.getValidationClass)
      }
      colProps.toMap
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

    "generate column defs with default validator correctly" in {
      val props = colProps(
        ColumnModel[String]("column1"),
        ColumnModel[String]("column2", indexed = true)
      )
      props shouldNot contain("column1")
      props should contain("column2" -> (true, ValidatorClass[String]))
    }

    "generate column defs with non-default validator correctly" in {
      val props = colProps(
        ColumnModel[Int]("column1"),
        ColumnModel[Int]("column2", indexed = true)
      )
      props should contain("column1" -> (false, ValidatorClass[Int]))
      props should contain("column2" -> (true, ValidatorClass[Int]))
    }
  }
}
