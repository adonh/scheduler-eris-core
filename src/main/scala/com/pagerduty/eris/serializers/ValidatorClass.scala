package com.pagerduty.eris.serializers

import com.netflix.astyanax.Serializer


/**
 * Allows to specify validatorClass which is used to define Cassandra schema.
 */
trait ValidatorClass {

  /**
   * Returns validator class string representing fully qualified cassandra validator type.
   * For example: {{{"org.apache.cassandra.db.marshal.UTF8Type"}}}
   *
   * @return validator class string
   */
  def validatorClass: String
}


object ValidatorClass {
  /**
   * Extract validator class from a given serializer.
   *
   * @param serializer target serializer
   * @tparam T optional type parameter to guide implicit serializer inference
   * @return validator class string
   */
  def apply[T](implicit serializer: Serializer[T]): String = serializer match {
    case c: ValidatorClass => c.validatorClass
    case s => s.getComparatorType.getClassName
  }
}
