package al.analyzer

import java.time.Duration

case class PowerReadingsAnalysisReport(timeForRelayToSwitch: Duration,
                                       timeForDeviceToTurnDown: Duration,
                                       testPassed: Boolean) {
  override def toString =
    s"""
       |Time it took for the relay to switch: ${timeForRelayToSwitch.toMillis} ms
       |Time it took for the device to turn down after the relay switched: ${timeForDeviceToTurnDown.toMillis} ms
       |Test passed: $testPassed
    """.stripMargin
}
