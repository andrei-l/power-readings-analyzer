package al.analyzer

import al.input.ReadingsInputParser

class PowerReadingsAnalyzer(readingsInputParser: ReadingsInputParser) {
  private val testStartedCondition = new TestStartedCondition
  private val testFinishedCondition = new TestFinishedCondition(testStartedCondition.lastReading)
  private val ffrEventTriggeredCondition = new FFREventTriggeredCondition
  private val relaySwitchedCondition = new RelaySwitchedInTimeCondition(ffrEventTriggeredCondition.lastReading)
  private val deviceShedInTimeCondition = new DeviceShedInTimeCondition(relaySwitchedCondition.lastReading)
  private val deviceWasTurnedDownForExpectedTimeCondition = new DeviceWasTurnedDownForExpectedTimeCondition(
    deviceShedInTimeCondition.lastKilowatts, deviceShedInTimeCondition.lastReading
  )
  private val deviceStartedRunningAgainCondition = new DeviceStartedRunningAgainCondition(
    deviceShedInTimeCondition.lastReading,
    deviceShedInTimeCondition.lastKilowatts,
    deviceWasTurnedDownForExpectedTimeCondition.lastReading
  )

  private val allConditions = Seq(
    testStartedCondition,
    testFinishedCondition,
    ffrEventTriggeredCondition,
    relaySwitchedCondition,
    deviceShedInTimeCondition,
    deviceWasTurnedDownForExpectedTimeCondition,
    deviceStartedRunningAgainCondition
  )

  def analyzeReadings(readingsLocation: String): PowerReadingsAnalysisReport = {
    readingsInputParser
      .parseInput(readingsLocation)
      .dropWhile(testStartedCondition.isNotSatisfied)
      .takeWhile(testFinishedCondition.isNotSatisfied)
      .dropWhile(ffrEventTriggeredCondition.isNotSatisfied)
      .dropWhile(relaySwitchedCondition.isNotSatisfied)
      .dropWhile(deviceShedInTimeCondition.isNotSatisfied)
      .dropWhile(deviceWasTurnedDownForExpectedTimeCondition.isNotSatisfied)
      .dropWhile(deviceStartedRunningAgainCondition.isNotSatisfied)
      .toList

    PowerReadingsAnalysisReport(
      timeForRelayToSwitch = duration(relaySwitchedCondition.lastReading, ffrEventTriggeredCondition.lastReading),
      timeForDeviceToTurnDown = duration(deviceShedInTimeCondition.kilowattsDecreasedTime, relaySwitchedCondition.lastReading),
      testPassed = allConditions.forall(_.conditionPassed))
  }
}
