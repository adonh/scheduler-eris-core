package com.pagerduty.eris

import com.eaio.uuid.UUIDGen
import java.util.UUID

/**
 * TimeUuid represents a subset of UUIDs that preserve temporal order. Since we cannot subclass
 * UUID directly, we use a wrapper.
 */
case class TimeUuid(val value: UUID) {

  /**
   * Time stamp when this UUID was created.
   */
  def timeStamp: Long = {
    (value.timestamp() - TimeUuid.UuidEpochOffsetIn100Ns) / 10000
  }

  override def toString() :String = value.toString()
}


object TimeUuid {

  /**
   * UUID time epoch starts in 1582... no joke.
   */
  private val UuidEpochOffsetIn100Ns = 0x01B21DD213814000L

  /**
   * Generates a unique UUID.
   */
  def apply() :TimeUuid = new TimeUuid(new UUID(UUIDGen.newTime(), UUIDGen.getClockSeqAndNode()))

  /**
   * Converts TimeUuid string representation into a TimeUuid object.
   */
  def apply(uuid: String) :TimeUuid = new TimeUuid(UUID.fromString(uuid))

  /**
   * Returns non unique id that can be used as a bound in ranged TimeUuid queries.
   */
  def nonUniqueLowerBound(timeStamp: Long) :TimeUuid = {
    require(timeStamp <= MaxTimeStamp, "'timeStamp' cannot be greater than MaxBound.timeStamp")

    // High bits of UUID.
    val uuidTime = timeStamp * 10000 + TimeUuid.UuidEpochOffsetIn100Ns

    val timeLow = (uuidTime << 32) // & 0xFFFFFFFF00000000L
    val timeMid = (uuidTime >> 16)    & 0x00000000FFFF0000L
    val version = 0x1000           // & 0x000000000000F000L
    val timeHi  = (uuidTime >> 48)    & 0x0000000000000FFFL

    val uuidMostSignificantBits = timeLow | timeMid | version | timeHi

    // Low bits are set to be the lowest value among all possible UUIDs in Cassandra.
    // UUIDs are compared as ByteBuffer, which in turn compares byte array as signed bytes.
    // So we use 0x80 = -128 to set the lowest value for all bytes.
    val uuidLeastSignificantBits = 0x8080808080808080L
    new TimeUuid(new UUID(uuidMostSignificantBits, uuidLeastSignificantBits))
  }

  /**
   * Minimum TimeUuid value, useful when doing ranged queries.
   */
  val MinBound = TimeUuid("00000000-0000-1000-8080-808080808080")

  /**
   * Maximum TimeUuid value, useful when doing ranged queries.
   */
  val MaxBound = TimeUuid("FFFFFFFF-FFFF-1FFF-7F7F-7F7F7F7F7F7F")
  private val MaxTimeStamp = MaxBound.timeStamp
}
