package com.pagerduty.eris.schema

/**
 * Common trait for all supported Repliation Strategies.
 */
sealed trait ReplicationStrategy {
  def strategyClass: String
  def options: Map[String, String]
}

/**
 * Simple replication strategy.
 *
 * @param replicationFactor number of replicas
 */
case class SimpleStrategy(replicationFactor: Int) extends ReplicationStrategy {
  val strategyClass = "org.apache.cassandra.locator.SimpleStrategy"
  val options = Map("replication_factor" -> replicationFactor.toString)
}

/**
 * Network topology aware replication strategy.
 *
 * @param topology a seq of tuples: datacenterName -> numberOfReplicas
 */
case class NetworkTopologyStrategy(topology: (String, Int)*) extends ReplicationStrategy {
  val strategyClass = "org.apache.cassandra.locator.NetworkTopologyStrategy"
  val options = topology.map { case (datacenterName, numberOfReplicas) =>
    datacenterName -> numberOfReplicas.toString
  }.toMap
}
