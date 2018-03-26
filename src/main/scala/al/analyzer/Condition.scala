package al.analyzer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import al.input.{ReadingsInput, RelayOn}

sealed trait TestCondition {
  def isSatisfied(readingsInput: ReadingsInput): Boolean
}


class TestStartingCondition extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean =
    readingsInput.frequency <= 50.01 && readingsInput.frequency >= 49.99
}

class TestStartedCondition(consecutiveInjectedFrequencies: Int) extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean = consecutiveInjectedFrequencies == 10
}

class FFREventTriggeredCondition extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean = readingsInput.frequency < 49.7
}

class RelaySwitchedInTimeCondition(readingsInputWhenConditionTriggered: ReadingsInput) extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean =
    (durationMillis(readingsInput.dateTime, readingsInputWhenConditionTriggered.dateTime) <= 400
      && readingsInput.relayStatus == RelayOn)
}

class DeviceShedInTimeCondition(startTimeOfTurnDown: LocalDateTime,
                                previousKilowatts: Int,
                                previousReadingsInput: ReadingsInput) extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean =
    (startTimeOfTurnDown.plus(30, ChronoUnit.SECONDS).isAfter(readingsInput.dateTime)
      && kilowatts(readingsInput, previousReadingsInput) == previousKilowatts)
}

class DeviceWasTurnedDownForExpectedTimeCondition(startTimeOfTurnDown: LocalDateTime,
                                                  kilowattsAtExpectedTurnDown: Int,
                                                  previousReadingsInput: ReadingsInput) extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean =
    (duration(readingsInput.dateTime, startTimeOfTurnDown).toMinutes <= 30
      && kilowatts(readingsInput, previousReadingsInput) == kilowattsAtExpectedTurnDown)
}

class DeviceStartedRunningAgainCondition(startTimeOfTurnDown: LocalDateTime,
                                         kilowattsAtExpectedTurnDown: Int,
                                         previousReadingsInput: ReadingsInput) extends TestCondition {
  override def isSatisfied(readingsInput: ReadingsInput): Boolean =
    (readingsInput.dateTime.isAfter(startTimeOfTurnDown.plus(30, ChronoUnit.MINUTES))
      && kilowattsAtExpectedTurnDown < kilowatts(readingsInput, previousReadingsInput))
}
