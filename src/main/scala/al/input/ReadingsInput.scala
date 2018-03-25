package al.input

import java.time.LocalDateTime

case class ReadingsInput(dateTime: LocalDateTime,
                         frequency: BigDecimal,
                         phase1: Int,
                         phase2: Int,
                         phase3: Int,
                         relayStatus: Option[RelayStatus])

sealed trait RelayStatus

object RelayOn extends RelayStatus
object RelayOff extends RelayStatus


object RelayStatus {
  def apply(v: String): RelayStatus = v match {
    case "on" => RelayOn
    case "off" => RelayOff
    case _ => throw new IllegalArgumentException("Invalid value for RelayStatus")
  }
}