package com.pagerduty.eris

import com.netflix.astyanax.connectionpool.ConnectionPoolMonitor
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl
import com.netflix.astyanax.thrift.ThriftFamilyFactory
import com.netflix.astyanax.{AstyanaxContext, Cluster}


/**
 * Cluster context combines all the Astyanax configuration as well as provides
 * startup() and shutdown() methods.
 *
 * The following example will help you setup connection to a locally running cassandra instance:
 * {{{
 * val clusterCtx = new ClusterCtx {
 *   val hosts: String = "localhost:9160"
 *   val clusterName: String = "CassCluster"
 *
 *   val astyanaxConfig = astyanaxConfigBuilder()
 *     .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
 *
 *   val connectionPoolConfig = connectionPoolConfigBuilder()
 *     .setPort(9160)
 *     .setMaxConnsPerHost(10)
 *
 *   val connectionPoolMonitor = new CountingConnectionPoolMonitor()
 * }
 * val cluster = clusterCtx.cluster
 * }}}
 */
trait ClusterCtx {

  val clusterName: String

  /**
   * Comma-delimited list of host:port pairs.
   */
  val hosts: String

  /**
   * See:
   * https://github.com/Netflix/astyanax/blob/master/astyanax-cassandra/src/main/java/com/netflix/astyanax/AstyanaxConfiguration.java
   */
  val astyanaxConfig: AstyanaxConfigurationImpl

  /**
   * See:
   * https://github.com/Netflix/astyanax/blob/master/astyanax-core/src/main/java/com/netflix/astyanax/connectionpool/ConnectionPoolConfiguration.java
   */
  val connectionPoolConfig: ConnectionPoolConfigurationImpl

  /**
   * Astyanax connection pool monitor. For example: {{{new CountingConnectionPoolMonitor()}}}.
   */
  val connectionPoolMonitor: ConnectionPoolMonitor

  /**
   * Creates a new Astyanax config builder that can be used a starting point
   * for the builder pattern.
   *
   * @return a new Astyanax config builder
   */
  protected def astyanaxConfigBuilder(): AstyanaxConfigurationImpl = {
    new AstyanaxConfigurationImpl()
  }

  /**
   * Creates a new connection pool builder that can be used a starting point
   * for the builder pattern.
   *
   * @return a new connection pool builder
   */
  protected def connectionPoolConfigBuilder(): ConnectionPoolConfigurationImpl = {
    new ConnectionPoolConfigurationImpl(clusterName + "ConnectionPool")
  }

  /**
   * Combines all the configs to create AstyanaxContext. Can be overridden for
   * further customization.
   */
  protected lazy val astyanaxCtx: AstyanaxContext[Cluster] = {
    new AstyanaxContext.Builder()
      .forCluster(clusterName)
      .withAstyanaxConfiguration(astyanaxConfig)
      .withConnectionPoolConfiguration(connectionPoolConfig.setSeeds(hosts))
      .withConnectionPoolMonitor(connectionPoolMonitor)
      .buildCluster(ThriftFamilyFactory.getInstance)
  }

  private lazy val startOnce: Boolean = {
    astyanaxCtx.start()
    true
  }

  /**
   * Starts the connection pool and other thread pools.
   */
  def start(): Unit = {
    startOnce
  }

  /**
   * Gracefully shuts down all the thraed pools.
   */
  def shutdown(): Unit = {
    astyanaxCtx.shutdown()
  }

  /**
   * Starts the Astyanax cluster and returns the running cluster instance.
   */
  lazy val cluster: Cluster = {
    start()
    astyanaxCtx.getClient()
  }
}
