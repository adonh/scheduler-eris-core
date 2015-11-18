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
 * val clusterCtx = new ClusterCtx(
 *   clusterName = "CassCluster",
 *   astyanaxConfig = new AstyanaxConfigurationImpl()
 *     .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE),
 *   connectionPoolConfig = new ConnectionPoolConfigurationImpl("CassConnectionPool")
 *     .setSeeds("localhost:9160")
 *     .setPort(9160),
 *   connectionPoolMonitor = new CountingConnectionPoolMonitor()
 * )
 * val cluster = clusterCtx.cluster
 * }}}
 */
class ClusterCtx(
    /**
     * Cluster name for logging and reporting purposes.
     */
    val clusterName: String,

    /**
     * See:
     * https://github.com/Netflix/astyanax/blob/master/astyanax-cassandra/src/main/java/com/netflix/astyanax/AstyanaxConfiguration.java
     */
    protected val astyanaxConfig: AstyanaxConfigurationImpl,

    /**
     * See:
     * https://github.com/Netflix/astyanax/blob/master/astyanax-core/src/main/java/com/netflix/astyanax/connectionpool/ConnectionPoolConfiguration.java
     */
    protected val connectionPoolConfig: ConnectionPoolConfigurationImpl,

    /**
     * Astyanax connection pool monitor. For example:
     * {{{
     *   new CountingConnectionPoolMonitor()
     * }}}.
     */
    protected val connectionPoolMonitor: ConnectionPoolMonitor)
{
  /**
   * Combines all the configs to create AstyanaxContext. Can be overridden for
   * further customization.
   */
  protected lazy val astyanaxCtx: AstyanaxContext[Cluster] = {
    new AstyanaxContext.Builder()
      .forCluster(clusterName)
      .withAstyanaxConfiguration(astyanaxConfig)
      .withConnectionPoolConfiguration(connectionPoolConfig)
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
   * Gracefully shuts down all the thread pools.
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
