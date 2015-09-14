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
  println('XXX1, System.identityHashCode(log), log.asInstanceOf[ch.qos.logback.classic.Logger].getEffectiveLevel)

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
    println('XXX2, System.identityHashCode(log), log.asInstanceOf[ch.qos.logback.classic.Logger].getEffectiveLevel)
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
  def describeKeyspace(cluster: Cluster, keyspaceName: String): String = {
    val description = cluster.describeKeyspace(keyspaceName).getProperties.toMap
    keyspaceName + ":\n" + description.map { case (key, value) =>
      s"  $key = $value"
    }.toSeq.sorted.mkString("\n")
  }
}
