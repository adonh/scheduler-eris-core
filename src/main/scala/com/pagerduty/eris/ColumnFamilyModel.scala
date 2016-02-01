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

import com.netflix.astyanax.ddl.{ ColumnFamilyDefinition, ColumnDefinition }
import com.netflix.astyanax.model.ColumnFamily
import com.netflix.astyanax.{ Cluster, Keyspace, Serializer }
import com.pagerduty.eris.serializers.ValidatorClass

/**
 * Keeps column family and related data together.
 */
class ColumnFamilyModel[RowKey, ColName, ColValue] protected (
    val keyspace: Keyspace,
    val columnFamily: ColumnFamily[RowKey, ColName],
    protected val settings: ColumnFamilySettings,
    protected val columns: Set[ColumnModel]
) {
  def name: String = columnFamily.getName

  def rowKeySerializer: Serializer[RowKey] = columnFamily.getKeySerializer
  def colNameSerializer: Serializer[ColName] = columnFamily.getColumnSerializer
  def colValueSerializer: Serializer[ColValue] = {
    columnFamily.getDefaultValueSerializer.asInstanceOf[Serializer[ColValue]]
  }

  /**
   * Provides schema definitions for this column family.
   *
   * @param cluster Astyanax cluster
   * @return column family definition
   */
  def columnFamilyDef(cluster: Cluster): ColumnFamilyDefinition = {
    val columnFamilyDef = cluster.makeColumnFamilyDefinition()
      .setKeyspace(keyspace.getKeyspaceName)
      .setName(name)
      .setKeyValidationClass(
        settings.rowKeyValidatorOverride.getOrElse(ValidatorClass(rowKeySerializer))
      )
      .setComparatorType(
        settings.colNameValidatorOverride.getOrElse(ValidatorClass(colNameSerializer))
      )
      .setDefaultValidationClass(
        settings.colValueValidatorOverride.getOrElse(ValidatorClass(colValueSerializer))
      )

    for (column <- columns) {
      columnFamilyDef.addColumnDefinition(column.columnDef(cluster))
    }
    columnFamilyDef
  }
}

object ColumnFamilyModel {

  /**
   * Creates a new column family model, using implicitly inferred serializers.
   *
   * Example: {{{
   *   import com.pagerduty.eris.serializers._
   *   val myCf = ColumnFamilyModel[String, TimeUuid, String](keyspace, "myCf")
   * }}}
   *
   * @param keyspace target keyspace
   * @param name column family name
   * @param settings addition schema settings
   * @param columns additional configuration for schema columns
   * @param rowKeySerializer serializer for row keys
   * @param colNameSerializer serializer for column names
   * @param colValueSerializer serializer for column values
   * @tparam RowKey optional type parameter to guide row key serializer inference
   * @tparam ColName optional type parameter to guide column name serializer inference
   * @tparam ColValue optional type parameter to guide column value serializer inference
   * @return a new column model
   */
  def apply[RowKey, ColName, ColValue](
    keyspace: Keyspace,
    name: String,
    settings: ColumnFamilySettings = new ColumnFamilySettings,
    columns: Set[ColumnModel] = Set.empty
  )(implicit
    rowKeySerializer: Serializer[RowKey],
    colNameSerializer: Serializer[ColName],
    colValueSerializer: Serializer[ColValue]): ColumnFamilyModel[RowKey, ColName, ColValue] =
    {
      val driverColumnFamily = new com.netflix.astyanax.model.ColumnFamily[RowKey, ColName](
        name,
        rowKeySerializer, colNameSerializer, colValueSerializer
      )

      new ColumnFamilyModel(keyspace, driverColumnFamily, settings, columns)
    }
}
