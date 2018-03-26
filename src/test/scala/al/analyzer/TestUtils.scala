package al.analyzer

import java.time.LocalDateTime

import al.input.{ReadingsInput, RelayOff, RelayStatus}

object TestUtils {
  def readingsInput(dateTime: LocalDateTime = LocalDateTime.now(),
                    frequency: BigDecimal = 0,
                    phase1: Int = 0,
                    phase2: Int = 0,
                    phase3: Int = 0,
                    relayStatus: RelayStatus = RelayOff): ReadingsInput =
    ReadingsInput(dateTime, frequency, phase1, phase2, phase3, relayStatus)
}
