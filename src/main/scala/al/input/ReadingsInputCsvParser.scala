package al.input

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.tototoshi.csv.CSVReader

import scala.util.{Failure, Success, Try}

class ReadingsInputCsvParser extends ReadingsInputParser {
  private final val DateTimePattern = "#(.*)".r
  private final val RelayStatusPattern = "(.*)\\$".r
  final val PowerReadingsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")

  override def parseInput(inputLocation: String): Stream[ReadingsInput] = {
    Try {
      CSVReader
        .open(new File(inputLocation))
        .toStream
        .map {
          case Seq(DateTimePattern(dateTime), frequency, _, _, _, _, phase1, phase2, phase3, _, _, RelayStatusPattern(relayStatus)) => ReadingsInput(
            LocalDateTime.parse(dateTime, PowerReadingsDateTimeFormatter),
            BigDecimal(frequency),
            phase1.toInt,
            phase2.toInt,
            phase3.toInt,
            Try(RelayStatus(relayStatus)).getOrElse(RelayOn)
          )
        }
    } match {
      case Success(stream) => stream
      case Failure(ex) => throw new IllegalArgumentException(s"Failed to load csv file by $inputLocation: ${ex.getMessage}", ex)
    }
  }
}
