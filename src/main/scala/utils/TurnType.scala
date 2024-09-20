package utils

object TurnType {
  def toInt(str: String) = {
    try {
      str.toInt
    } catch {
      case _: Exception => -1
    }
  }
  def toDouble(str: String) = {
    try {
      str.toDouble
    } catch {
      case _: Exception => -1.0
    }
  }
}
