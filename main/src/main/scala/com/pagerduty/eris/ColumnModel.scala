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

import com.netflix.astyanax.ddl.ColumnDefinition
import com.netflix.astyanax.{ Cluster, Serializer }
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
  def apply[T](name: String, indexed: Boolean = false)(implicit serializer: Serializer[T]): ColumnModel = {
    ColumnModel(name, indexed, ValidatorClass(serializer))
  }
}
