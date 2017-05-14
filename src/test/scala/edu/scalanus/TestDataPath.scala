package edu.scalanus

import java.io.File
import java.nio.file.Paths

trait TestDataPath {
  protected val EXT = "scl"

  protected def basePath: String = ???

  protected val baseTestDataPath = "src/test/resources"

  protected val testDataPath: String = s"$baseTestDataPath/$basePath"

  protected lazy val testDataDir: File = Paths.get(testDataPath).toFile
}

trait TestDataPathWithResult extends TestDataPath {
  protected def resultPath: String = s"${Paths.get(basePath).getParent.toString}/results"

  protected val resultDataPath: String = s"$baseTestDataPath/$resultPath"

  protected lazy val resultDataDir: File = Paths.get(resultDataPath).toFile
}
