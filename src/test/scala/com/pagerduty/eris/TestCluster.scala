package com.pagerduty.eris

import ch.qos.logback.classic.{Level, Logger}
import com.netflix.astyanax.connectionpool.NodeDiscoveryType
import com.netflix.astyanax.connectionpool.impl.{
  ConnectionPoolMBeanManager, ConnectionPoolConfigurationImpl, CountingConnectionPoolMonitor}
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl
import com.netflix.astyanax.thrift.ThriftKeyspaceImpl
import com.pagerduty.eris.schema.SchemaLoader
import org.slf4j.LoggerFactory


/**
 * Cluster context used for testing.
 */
object TestClusterCtx extends ClusterCtx(
    clusterName = "CassCluster",
    astyanaxConfig = new AstyanaxConfigurationImpl()
      .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE),
    connectionPoolConfig = new ConnectionPoolConfigurationImpl("CassConnectionPool")
      .setSeeds("localhost:9160")
      .setPort(9160),
    connectionPoolMonitor = new CountingConnectionPoolMonitor()
  )
{
  TestLoggingConfig.setup()
}

object TestLoggingConfig {
  /**
   * Removes noise from test logs.
   */
  def setup(): Unit = {
    implicit def asConcreteLogger(logger: org.slf4j.Logger) = logger.asInstanceOf[Logger]
    LoggerFactory.getLogger(classOf[SchemaLoader].getName).setLevel(Level.ERROR)
    LoggerFactory.getLogger(classOf[CountingConnectionPoolMonitor].getName).setLevel(Level.WARN)
    LoggerFactory.getLogger(classOf[ConnectionPoolMBeanManager].getName).setLevel(Level.WARN)
    LoggerFactory.getLogger(classOf[ThriftKeyspaceImpl].getName).setLevel(Level.WARN)
  }
}
