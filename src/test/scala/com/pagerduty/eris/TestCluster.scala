package com.pagerduty.eris

import com.netflix.astyanax.connectionpool.NodeDiscoveryType
import com.netflix.astyanax.connectionpool.impl.{ConnectionPoolConfigurationImpl, CountingConnectionPoolMonitor}
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl


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
