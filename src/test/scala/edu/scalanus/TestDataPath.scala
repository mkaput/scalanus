package edu.scalanus

import java.io.File
import java.nio.file.Paths

trait TestDataPath {
  protected val EXT = "scl"

  protected def basePath: String

  protected val baseTestDataPath = "src/test/resources"

  protected val testDataPath: String = s"$baseTestDataPath/$basePath"

  protected lazy val testDataDir: File = Paths.get(testDataPath).toFile
}
