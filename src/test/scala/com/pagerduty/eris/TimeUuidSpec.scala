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

    "do bound checks" in {
      val maxTimeStamp = TimeUuid.MaxBound.timeStamp
      val withMaxUnixEpochTimeStamp = TimeUuid("ffffe4c0-ffff-1fff-8080-808080808080")
      TimeUuid.nonUniqueLowerBound(maxTimeStamp) shouldBe withMaxUnixEpochTimeStamp
      intercept[IllegalArgumentException] { TimeUuid.nonUniqueLowerBound(maxTimeStamp + 1) }

      val minTimeStamp = TimeUuid.MinBound.timeStamp
      val withMinUnixEpochTimeStamp = TimeUuid("00000000-0000-1000-8080-808080808080")
      TimeUuid.nonUniqueLowerBound(minTimeStamp) shouldBe withMinUnixEpochTimeStamp
      intercept[IllegalArgumentException] { TimeUuid.nonUniqueLowerBound(minTimeStamp - 1) }
    }
  }
}
