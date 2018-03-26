package al.analyzer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import org.scalatest.{Matchers, WordSpec}
import TestUtils.readingsInput

class packageSpec extends WordSpec with Matchers {

  "package utils" should {
    "calculate kilowatts properly" in {
      val now = LocalDateTime.now()
      val nowPlusHalfSecond = now.plus(500, ChronoUnit.MILLIS)
      val oldReadings = readingsInput(dateTime = now, phase1 = 60, phase2 = 20, phase3 = 20)
      val currentReadings = oldReadings.copy(dateTime = nowPlusHalfSecond, phase3 = 21)
      kilowatts(currentReadings, oldReadings) shouldEqual 2
    }
  }

}
