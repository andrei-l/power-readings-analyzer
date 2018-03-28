package al

import java.time.{Duration, LocalDateTime}

import al.input.ReadingsInput

import scala.language.implicitConversions

package object analyzer {
  type Kilowatt = Int

  def kilowatts(currentReadings: ReadingsInput, previousReadings: ReadingsInput): Kilowatt =
    ((totalPower(currentReadings) - totalPower(previousReadings)) * 1000
      / durationMillis(currentReadings.dateTime, previousReadings.dateTime)).intValue()

  private def totalPower(readingsInput: ReadingsInput): BigDecimal =
    readingsInput.phase1 + readingsInput.phase2 + readingsInput.phase3

  def durationMillis(currentReadingsTime: LocalDateTime, previousReadingsTime: LocalDateTime): BigDecimal =
    duration(previousReadingsTime, currentReadingsTime).toMillis

  def duration(currentReadingsTime: LocalDateTime, previousReadingsTime: LocalDateTime): Duration =
    Duration.between(previousReadingsTime, currentReadingsTime).abs()


  def duration(maybeCurrentReadingsTime: Option[LocalDateTime], maybePreviousReadingsTime: Option[LocalDateTime]): Duration =
    (for {
      maybeCurrentReadingsTime <- maybeCurrentReadingsTime
      previousReadingsTime <- maybePreviousReadingsTime
    } yield duration(maybeCurrentReadingsTime, previousReadingsTime)).getOrElse(Duration.ZERO)


  implicit def optionReadingsInputToOptionDateTime(maybeReadingsInput: Option[ReadingsInput]): Option[LocalDateTime] =
    maybeReadingsInput.map(_.dateTime)

  def isTrue: Boolean => Boolean = _ == true
}
