package al.analyzer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

import al.analyzer.TestUtils.readingsInput
import al.input.{ReadingsInput, RelayOff, RelayOn}
import org.scalatest.{Matchers, WordSpec}

class TestConditionSpec extends WordSpec with Matchers {

  "Conditions" should {
    "determine test is starting" in {
      val condition = TestStartingCondition
      condition.isSatisfied(readingsInput(frequency = 49.99)) shouldEqual true
      condition.isSatisfied(readingsInput(frequency = 50.00)) shouldEqual true
      condition.isSatisfied(readingsInput(frequency = 50.01)) shouldEqual true
      condition.isSatisfied(readingsInput(frequency = 50.02)) shouldEqual false
      condition.isSatisfied(readingsInput(frequency = 49.9)) shouldEqual false
    }

    "determine test has started" in {
      val condition1 = new TestStartedCondition()
      (0 to 5)
        .map(_ => condition1.isSatisfied(readingsInput(frequency = 50.00)))
        .reduce((_, _) => condition1.conditionPassed) shouldEqual false

      val condition2 = new TestStartedCondition()
      (0 to 11)
        .map(_ => condition2.isSatisfied(readingsInput(frequency = 50.00)))
        .reduce((_, _) => condition2.conditionPassed) shouldEqual true

      val condition3 = new TestStartedCondition()
      (0 to 10)
        .map(_ => condition3.isSatisfied(readingsInput(frequency = 49.00)))
        .reduce((_, _) => condition3.conditionPassed) shouldEqual false
    }

    "determine FFR event has been triggered" in {
      val condition = new FFREventTriggeredCondition()
      val ffrReading = readingsInput(frequency = 49.6)
      condition.isSatisfied(readingsInput(frequency = 49.7)) shouldEqual false
      condition.isSatisfied(readingsInput(frequency = 50.7)) shouldEqual false
      condition.isSatisfied(ffrReading) shouldEqual true
      condition.isSatisfied(readingsInput(frequency = 47.6)) shouldEqual true

      condition.lastReading shouldEqual Some(ffrReading)
      condition.lastKilowatts shouldEqual None
    }

    "determine relay switched within 400ms" in {
      val now = LocalDateTime.now()
      val nowPlus400 = now.plus(400, ChronoUnit.MILLIS)
      val nowPlus500 = now.plus(500, ChronoUnit.MILLIS)
      val condition = new RelaySwitchedInTimeCondition(readingsInput(dateTime = now))
      val relaySwitchReading = readingsInput(dateTime = nowPlus400, relayStatus = RelayOn)
      condition.isSatisfied(relaySwitchReading) shouldEqual true
      condition.isSatisfied(readingsInput(dateTime = nowPlus500, relayStatus = RelayOn)) shouldEqual false
      condition.isSatisfied(readingsInput(dateTime = nowPlus400, relayStatus = RelayOff)) shouldEqual false

      condition.lastReading shouldEqual Some(relaySwitchReading)
    }

    "determine device shed within 30 seconds" in {
      val now = LocalDateTime.now()
      val nowPlus29 = now.plus(29, ChronoUnit.SECONDS)
      val nowPlus31 = now.plus(31, ChronoUnit.SECONDS)
      val condition = new DeviceShedInTimeCondition(readingsInput(dateTime = now, phase1 = 1, phase2 = 1, phase3 = 1))
      val lastInShed = readingsInput(nowPlus31, phase1 = 120, phase2 = 200, phase3 = 200)
      condition.isSatisfied(readingsInput(nowPlus29, phase1 = 200, phase2 = 200, phase3 = 200)) shouldEqual false
      condition.isSatisfied(readingsInput(nowPlus29, phase1 = 120, phase2 = 100, phase3 = 100)) shouldEqual false
      condition.isSatisfied(lastInShed) shouldEqual true

      condition.lastReading shouldEqual Some(lastInShed)
      condition.lastKilowatts shouldEqual Some(10)
    }

    "determine device keeps turned down for 30 minutes" in {
      val now = LocalDateTime.now()
      val nowPlus29 = now.plus(29, ChronoUnit.MINUTES)
      val nowPlus31 = now.plus(31, ChronoUnit.MINUTES)
      val condition = new DeviceWasTurnedDownForExpectedTimeCondition(10, readingsInput(dateTime = now, phase1 = 100, phase2 = 100, phase3 = 100))
      val lastReading = readingsInput(nowPlus29, phase1 = 10000, phase2 = 8000, phase3 = 1000)
      condition.isSatisfied(readingsInput(nowPlus31, phase1 = 10000, phase2 = 8000, phase3 = 1000)) shouldEqual false
      condition.isSatisfied(readingsInput(nowPlus29, phase1 = 12000, phase2 = 8000, phase3 = 1000)) shouldEqual false
      condition.isSatisfied(lastReading) shouldEqual true

      condition.lastReading shouldEqual Some(lastReading)
      condition.lastKilowatts shouldEqual Some(10)
    }

    "determine device started running again after 30 minutes" in {
      val now = LocalDateTime.now()
      val nowPlus29 = now.plus(29, ChronoUnit.MINUTES)
      val nowPlus31 = now.plus(31, ChronoUnit.MINUTES)
      val condition = new DeviceStartedRunningAgainCondition(now, 10, readingsInput(dateTime = now, phase1 = 100, phase2 = 100, phase3 = 100))
      condition.isSatisfied(readingsInput(nowPlus29, phase1 = 20000, phase2 = 8000, phase3 = 4000)) shouldEqual false
      condition.isSatisfied(readingsInput(nowPlus31, phase1 = 10000, phase2 = 8000, phase3 = 1000)) shouldEqual false
      condition.isSatisfied(readingsInput(nowPlus31, phase1 = 20000, phase2 = 8000, phase3 = 4000)) shouldEqual true
    }
  }

  implicit def readingsToOptionReadings(readingsInput: ReadingsInput): Option[ReadingsInput] = Some(readingsInput)

  implicit def kilowattsToOptionKilowatts(kilowatts: Kilowatt): Option[Kilowatt] = Some(kilowatts)

  implicit def localDateTimeToOptionLocalDateTime(localDateTime: LocalDateTime): Option[LocalDateTime] = Some(localDateTime)
}
