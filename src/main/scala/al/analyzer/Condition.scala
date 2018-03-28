package al.analyzer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import cats._
import cats.data._
import cats.implicits._

import al.input.{ReadingsInput, RelayOn}

sealed trait TestCondition {
  protected var _lastReading: Option[ReadingsInput] = None
  protected var _lastKilowatts: Option[Kilowatt] = None
  private var _satisfied: Boolean = false

  def lastReading: Option[ReadingsInput] = _lastReading

  def lastKilowatts: Option[Kilowatt] = _lastKilowatts

  def conditionPassed: Boolean = _satisfied

  protected def _isSatisfied(readingsInput: ReadingsInput): Boolean

  def isSatisfied(readingsInput: ReadingsInput): Boolean = {
    _satisfied = _isSatisfied(readingsInput)
    _satisfied
  }

  def isNotSatisfied: ReadingsInput => Boolean = !isSatisfied(_)
}


object TestStartingCondition extends TestCondition {
  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean =
    readingsInput.frequency <= 50.01 && readingsInput.frequency >= 49.99
}

class TestStartedCondition extends TestCondition {
  private var consecutiveInjectedFrequencies = 0

  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    if (TestStartingCondition.isSatisfied(readingsInput)) {
      consecutiveInjectedFrequencies += 1
    }
    _lastReading = Some(readingsInput)
    consecutiveInjectedFrequencies >= 10
  }
}

class TestFinishedCondition(maybePreviousReadings: => Option[ReadingsInput]) extends TestCondition {
  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean =
    maybePreviousReadings.forall(previousReadings => duration(readingsInput.dateTime, previousReadings.dateTime).toMinutes >= 35)
}

class FFREventTriggeredCondition extends TestCondition {
  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    val satisfied = readingsInput.frequency < 49.7
    if (satisfied) {
      _lastReading = Some(readingsInput)
    }
    satisfied
  }
}

class RelaySwitchedInTimeCondition(maybeReadingsInputWhenFFREventTriggered: => Option[ReadingsInput]) extends TestCondition {

  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    val relaySwitched = maybeReadingsInputWhenFFREventTriggered.forall { readingsInputWhenFFREventTriggered =>
      (readingsInput != readingsInputWhenFFREventTriggered &&
        durationMillis(readingsInput.dateTime, readingsInputWhenFFREventTriggered.dateTime) <= 400
        && readingsInput.relayStatus == RelayOn)
    }
    if (relaySwitched) {
      _lastReading = Some(readingsInput)
      _lastKilowatts = Some(kilowatts(readingsInput, maybeReadingsInputWhenFFREventTriggered.get))
    }
    relaySwitched
  }
}

class DeviceShedInTimeCondition(maybePreviousReadingsInput: => Option[ReadingsInput]) extends TestCondition {
  private var _maybePreviousReadingsInput: Option[ReadingsInput] = None
  private var _kilowattsDecreasedReadins: Option[ReadingsInput] = None

  def kilowattsDecreasedTime: Option[ReadingsInput] = _kilowattsDecreasedReadins

  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    _maybePreviousReadingsInput = Seq(_maybePreviousReadingsInput, maybePreviousReadingsInput).collectFirst {
      case Some(previousReading) if previousReading != readingsInput => previousReading
    }
    val maybeKilowatts = _maybePreviousReadingsInput.map(previousReadings => kilowatts(readingsInput, previousReadings))
    val previousSavedKilowatts = _lastKilowatts

    val nestedKilowatts = Nested(List(_lastKilowatts, maybeKilowatts))
    _lastKilowatts = Foldable[Nested[List, Option, ?]].minimumOption(nestedKilowatts)
    _lastReading = Some(readingsInput)
    _kilowattsDecreasedReadins =
      if (maybeKilowatts != previousSavedKilowatts && _lastKilowatts == maybeKilowatts) Some(readingsInput) else _kilowattsDecreasedReadins

    _maybePreviousReadingsInput.exists(_.dateTime.plus(30, ChronoUnit.SECONDS).isBefore(readingsInput.dateTime))
  }
}

class DeviceWasTurnedDownForExpectedTimeCondition(maybeKilowattsAtExpectedTurnDown: => Option[Kilowatt],
                                                  maybePreviousReadingsInput: => Option[ReadingsInput]) extends TestCondition {
  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    val turnedDownForExpectedTime = (for {
      kilowattsAtExpectedTurnDown <- maybeKilowattsAtExpectedTurnDown
      previousReadingsInput <- maybePreviousReadingsInput
    } yield (readingsInput != previousReadingsInput &&
      duration(readingsInput.dateTime, previousReadingsInput.dateTime).toMinutes <= 30
      && kilowatts(readingsInput, previousReadingsInput) == kilowattsAtExpectedTurnDown)).exists(isTrue)

    if (turnedDownForExpectedTime) {
      _lastReading = Some(readingsInput)
      _lastKilowatts = Some(kilowatts(readingsInput, maybePreviousReadingsInput.get))
    }
    turnedDownForExpectedTime
  }
}

class DeviceStartedRunningAgainCondition(maybeStartTimeOfTurnDown: => Option[LocalDateTime],
                                         maybeKilowattsAtExpectedTurnDown: => Option[Kilowatt],
                                         maybePreviousReadingsInput: => Option[ReadingsInput]) extends TestCondition {

  protected override def _isSatisfied(readingsInput: ReadingsInput): Boolean = {
    (for {
      startTimeOfTurnDown <- maybeStartTimeOfTurnDown
      kilowattsAtExpectedTurnDown <- maybeKilowattsAtExpectedTurnDown
      previousReadingsInput <- maybePreviousReadingsInput
    } yield (readingsInput.dateTime.isAfter(startTimeOfTurnDown.plus(30, ChronoUnit.MINUTES))
      && kilowattsAtExpectedTurnDown < kilowatts(readingsInput, previousReadingsInput))).exists(isTrue)
  }
}

