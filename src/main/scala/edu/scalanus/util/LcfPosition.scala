package edu.scalanus.util

/**
  * Line-column-file name position in source text.
  */
case class LcfPosition(
  lineNumber: Int,
  columnNumber: Int = -1,
  fileName: String = null
) {

  if (lineNumber < 0) throw new IllegalArgumentException("lineNumber")
  if (columnNumber < -1) throw new IllegalArgumentException("columnNumber")

  override def toString: String = {
    val sb = new StringBuilder

    if (fileName != null && !fileName.isEmpty) {
      sb.append(fileName).append(':')
    } else {
      sb.append("line ")
    }

    sb.append(lineNumber)

    if (columnNumber != -1) {
      sb.append(':').append(columnNumber)
    }

    sb.toString
  }

}
