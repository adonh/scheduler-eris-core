package com.pagerduty.eris

import com.netflix.astyanax.{Cluster, Serializer}
import com.netflix.astyanax.ddl.ColumnDefinition
import com.pagerduty.eris.serializers.ValidatorClass

/**
 * High level column declaration that can be converted to Astyanax column definition.
 *
 * @param name column name
 * @param indexed true for indexed columns, false otherwise
 * @param validationClass validation class for the column value
 */
case class ColumnModel(name: String, indexed: Boolean, validationClass: String) {

  /**
   * Converts this column declaration into Astyanax column definition.
   *
   * @param cluster target cluster
   * @return Astyanax column definition
   */
  def columnDef(cluster: Cluster): ColumnDefinition = {
    val colDef = cluster.makeColumnDefinition()
      .setName(name)
      .setValidationClass(validationClass)

    if (indexed) colDef.setKeysIndex()
    colDef
  }
}

object ColumnModel {

  /**
   * Simplified factory that uses implicit serializers to get validationClass for a given type.
   *
   * @param name column name
   * @param indexed true for indexed columns, false otherwise
   * @param serializer serializer used to derive validationClass
   * @tparam T option type parameter to guide implicit serializer inference
   * @return a new column declaration
   */
  def apply[T](name: String, indexed: Boolean = false)(implicit serializer: Serializer[T])
  : ColumnModel = {
    ColumnModel(name, indexed, ValidatorClass(serializer))
  }
}
