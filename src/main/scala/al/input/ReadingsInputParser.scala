package al.input

trait ReadingsInputParser {
  def parseInput(inputLocation: String): Stream[ReadingsInput]
}
