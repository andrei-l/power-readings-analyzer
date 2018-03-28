package al

import al.analyzer.PowerReadingsAnalyzer
import al.input.{ReadingsInputCsvParser, ReadingsInputParser}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

object AppRunner extends App with LazyLogging {

  private val readingsInputParser:ReadingsInputParser = new ReadingsInputCsvParser
  private val powerReadingsAnalyzer: PowerReadingsAnalyzer = new PowerReadingsAnalyzer(readingsInputParser)

  Try {
    assert(args.length == 1, "Please run application with 1 argument: java -jar [jar_file] [readings_input_csv_path] ")
    powerReadingsAnalyzer.analyzeReadings(args(0))
  } match {
    case Success(analysisReport) => logger.info(s"Successfully analyzed power readings: $analysisReport")
    case Failure(ex) =>logger.error(s"Failed run rate calculation system: ${ex.getMessage}", ex)
  }
}
