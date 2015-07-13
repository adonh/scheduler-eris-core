package com.pagerduty.eris

/**
 * Allows to override validators.
 *
 * For example, you can use the following settings to create counter column family.
 * {{{
 * ColumnFamilySettings(colValueValidatorOverride = Some(
 *   com.netflix.astyanax.serializers.ComparatorType.COUNTERTYPE.getClassName))
 * }}}
 */
// May be extended to accommodate other column settings in the future.
case class ColumnFamilySettings(
    rowKeyValidatorOverride: Option[String] = None,
    colNameValidatorOverride: Option[String] = None,
    colValueValidatorOverride: Option[String] = None)
