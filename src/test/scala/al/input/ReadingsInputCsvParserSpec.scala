package al.input

import java.time.LocalDateTime

import org.scalatest.{Matchers, WordSpec}

class ReadingsInputCsvParserSpec extends WordSpec with Matchers {

  private val parser = new ReadingsInputCsvParser


  "The new ReadingsInputCsvParser" should {
    "parse input CSV file when its location is correct" in {
      val parsedReadings = parser.parseInput(getClass.getClassLoader.getResource("parsing-test-input.csv").getPath)
      parsedReadings shouldEqual Seq(
        ReadingsInput(
          LocalDateTime.parse("2016-10-20 15:01:48:251", parser.PowerReadingsDateTimeFormatter),
          BigDecimal(49.7004),
          1618813,
          1611729,
          1576752,
          Some(RelayOff)
        ),

        ReadingsInput(
          LocalDateTime.parse("2016-10-20 15:01:48:351", parser.PowerReadingsDateTimeFormatter),
          BigDecimal(49.6991),
          1618824,
          1611741,
          1576764,
          Some(RelayOn)
        ),

        ReadingsInput(
          LocalDateTime.parse("2016-10-20 15:01:48:851", parser.PowerReadingsDateTimeFormatter),
          BigDecimal(49.6903),
          1618881,
          1611797,
          1576818,
          None
        )
      )
    }
  }


}
