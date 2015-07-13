package com.pagerduty.eris.core

import java.nio.ByteBuffer

import com.pagerduty.eris.TimeUuid
import org.scalatest.{Matchers, FreeSpec}


class TimeUuidSpec extends FreeSpec with Matchers {
  "TimeUuid should" - {
    "return correct timeStamp" in {
      val deltas = for (i <- 0 until 20) yield {
        System.currentTimeMillis() - TimeUuid().timeStamp
      }
      deltas.forall(_ < 1) shouldBe true // If you fail this test, I am truly sorry for you!
      deltas.exists(_ == 0) shouldBe true
    }

    "convert to and from string" in {
      val a = TimeUuid()
      val b = TimeUuid(a.toString)
      a shouldBe b
    }

    "generate correct non-unique lower bound" in {
      val timeStamp = System.currentTimeMillis() - 1000

      val bound = TimeUuid.nonUniqueLowerBound(timeStamp)
      bound shouldBe TimeUuid.nonUniqueLowerBound(timeStamp)
      bound.timeStamp shouldBe timeStamp

      val bytes = ByteBuffer.allocate(64 / 8).putLong(bound.value.getLeastSignificantBits).array()
      bytes.forall(_ == Byte.MinValue)
    }
  }
}
