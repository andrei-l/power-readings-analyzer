package al.input

import java.time.LocalDateTime

case class ReadingsInput(dateTime: LocalDateTime,
                         frequency: BigDecimal,
                         phase1: Int,
                         phase2: Int,
                         phase3: Int,
                         relayStatus: RelayStatus) {
  val totalPower: Int = phase1 + phase2 + phase3
}

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