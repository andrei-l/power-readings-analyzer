package al.input

import org.scalatest.{Matchers, WordSpec}

class RelayStatusSpec extends WordSpec with Matchers {

  "The RelayStatus" should {
    "be created from 'on' and 'off' values" in {
      RelayStatus("on") shouldEqual RelayOn
      RelayStatus("off") shouldEqual RelayOff
    }

    "fail with error while trying to create itself with invalid value" in {

      assertThrows[IllegalArgumentException] {
        RelayStatus("something")
      }
    }
  }

}
