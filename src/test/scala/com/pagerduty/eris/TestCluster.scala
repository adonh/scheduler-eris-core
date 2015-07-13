package com.pagerduty.eris

import com.netflix.astyanax.connectionpool.NodeDiscoveryType
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor


object TestCluster {

  lazy val cluster = {
    val clusterCtx = new ClusterCtx {
      lazy val hosts: String = "localhost:9160"
      lazy val clusterName: String = "TestCluster"

      lazy val astyanaxConfig = astyanaxConfigBuilder()
        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)

      lazy val connectionPoolConfig = connectionPoolConfigBuilder()
        .setPort(9160)
        .setMaxConnsPerHost(10)

      lazy val connectionPoolMonitor = new CountingConnectionPoolMonitor()
    }

    clusterCtx.cluster
  }
}
