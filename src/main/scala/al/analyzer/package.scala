package al

import java.time.{Duration, LocalDateTime}

import al.input.ReadingsInput

package object analyzer {
  def kilowatts(currentReadings: ReadingsInput, previousReadings: ReadingsInput): Int =
    ((totalPower(currentReadings) - totalPower(previousReadings)) * 1000
      / durationMillis(currentReadings.dateTime, previousReadings.dateTime)).intValue()

  private def totalPower(readingsInput: ReadingsInput): BigDecimal =
    readingsInput.phase1 + readingsInput.phase2 + readingsInput.phase3

  def durationMillis(currentReadingsTime: LocalDateTime, previousReadingsTime: LocalDateTime): BigDecimal =
    duration(previousReadingsTime, currentReadingsTime).toMillis

  def duration(currentReadingsTime: LocalDateTime, previousReadingsTime: LocalDateTime): Duration =
    Duration.between(previousReadingsTime, currentReadingsTime).abs()
}
