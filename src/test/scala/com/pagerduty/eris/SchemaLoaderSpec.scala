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

import com.netflix.astyanax.Cluster
import com.netflix.astyanax.ddl.ColumnFamilyDefinition
import com.pagerduty.eris.schema.SchemaLoader
import com.pagerduty.eris.serializers._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FreeSpec, Matchers }

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

    val keyspaceName = "SchemaLoaderSpec1"

    def colProps(columns: ColumnModel*): Map[String, (Boolean, String)] = {
      val cluster = (new TestClusterCtx).cluster
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
