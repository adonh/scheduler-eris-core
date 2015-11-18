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

package com.pagerduty.eris.schema

import com.netflix.astyanax.Cluster
import com.netflix.astyanax.ddl.ColumnFamilyDefinition
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.util.control.NonFatal

/**
 * SchemaLoader allows to load and drop database schema. Useful for tests and for creating
 * production schema prototypes that can then be tweaked and loaded using the database client.
 */
class SchemaLoader(
    /** Target cluster. */
    val cluster: Cluster,
    /** A set of all column family definitions. */
    val columnFamilyDefs: Set[ColumnFamilyDefinition],
    /** A map of keyspace names to corresponding replication strategy. */
    val replicationStrategies: Map[String, ReplicationStrategy] =
      Map.empty.withDefaultValue(SimpleStrategy(1)))
{
  private val log = LoggerFactory.getLogger(classOf[SchemaLoader])

  private case class KeyspaceSettings(
    name: String,
    columnFamilyDefs: Set[ColumnFamilyDefinition],
    replicationStrategy: ReplicationStrategy)

  private val keyspaces: Set[KeyspaceSettings] = {
    columnFamilyDefs.groupBy(_.getKeyspace).map { case (ksName, cfDefs) =>
      KeyspaceSettings(ksName, cfDefs, replicationStrategies(ksName))
    }.toSet
  }

  /**
   * Loads schema into the database.
   */
  def loadSchema(): Unit = {
    for (keyspace <- keyspaces) {
      log.warn(s"Loading schema for keyspace '${keyspace.name}'.")
      val keyspaceDef = cluster.makeKeyspaceDefinition()
        .setName(keyspace.name)
        .setStrategyClass(keyspace.replicationStrategy.strategyClass)
        .setStrategyOptions(keyspace.replicationStrategy.options)

      cluster.addKeyspace(keyspaceDef)

      for (columnFamilyDef <- keyspace.columnFamilyDefs) {
        cluster.addColumnFamily(columnFamilyDef)
      }
    }
  }

  /**
   * Drops the schema (will erase all data).
   */
  def dropSchema(): Unit = {
    for (keyspace <- keyspaces) {
      SchemaLoader.dropKeyspace(cluster, keyspace.name)
    }
  }
}


object SchemaLoader {

  /**
   * Drops the target keyspace.
   *
   * @param cluster target cluster
   * @param keyspaceName keyspace name
   */
  def dropKeyspace(cluster: Cluster, keyspaceName: String): Unit = {
    val log = LoggerFactory.getLogger(classOf[SchemaLoader])
    log.warn(s"Dropping keyspace '$keyspaceName'.")
    try {
      cluster.dropKeyspace(keyspaceName)
    }
    catch {
      case NonFatal(_) =>
        log.warn( s"Unable to drop keyspace '$keyspaceName'.")
    }
  }

  /**
   * Provides a string dump of the keyspace description.
   *
   * @param cluster target cluster
   * @param keyspaceName keyspace name
   * @return a formatted string describing selected keyspace properties
   */
  def describeKeyspace(cluster: Cluster, keyspaceName: String): Option[String] = {
    Option(cluster.describeKeyspace(keyspaceName)).map { result =>
      val description = result.getProperties.toMap
      keyspaceName + ":\n" + description.map { case (key, value) =>
        s"  $key = $value"
      }.toSeq.sorted.mkString("\n")
    }
  }
}
