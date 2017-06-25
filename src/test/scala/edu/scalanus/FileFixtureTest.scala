package edu.scalanus

import java.io.{File, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

trait FileFixtureTest extends TestDataPath {

  protected def resultPath: String = s"${Paths.get(basePath).getParent.toString}/results"

  protected val resultDataPath: String = s"$baseTestDataPath/$resultPath"

  protected lazy val resultDataDir: File = Paths.get(resultDataPath).toFile

  protected def fixtureExtensions: Array[String] = Array(EXT)

  protected lazy val fixtures: Seq[File] = findFixtures(testDataDir, fixtureExtensions)

  protected def getFixtureName(fixtureFile: File): String = {
    fixtureFile.getAbsolutePath.stripPrefix(testDataDir.getAbsolutePath).stripPrefix(File.separator)
  }

  protected def getOrCreateResults(fixture: File, actual: String): String = {
    val resultPath = Paths.get(s"$resultDataPath/${stripExtension(getFixtureName(fixture))}.txt")
    val f = resultPath.toFile
    if (f.isFile) {
      Source.fromFile(f).mkString
    } else {
      System.err.println(s"WARNING: Couldn't find results file for ${getFixtureName(fixture)}, creating new one")
      val p = f.getParentFile
      if (!p.exists()) {
        if (!p.mkdirs()) {
          throw new IOException(s"Failed to create directory ${p.getAbsolutePath}")
        }
      } else if (p.isFile) {
        throw new IOException(s"Cannot make directory ${p.getAbsolutePath}")
      }

      Files.write(resultPath, actual.getBytes())

      actual
    }
  }

  protected def findFixtures(f: File, ext: Array[String]): Seq[File] = {
    val buffer = ArrayBuffer.empty[File]
    if (f.isDirectory) {
      try {
        Files.walkFileTree(f.toPath, new SimpleFileVisitor[Path] {
          override def visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult = {
            val f = path.toFile
            if (f.isFile && ext.contains(getExtension(path.toString))) {
              buffer += f
            }
            FileVisitResult.CONTINUE
          }
        })
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
    buffer
  }

  private def stripExtension(pathname: String): String = {
    if (pathname != null) {
      val dot = pathname.lastIndexOf(".")
      if (dot != -1 && dot + 1 < pathname.length - 1)
        return pathname.substring(0, dot)
    }
    ""
  }

  private def getExtension(pathname: String): String = {
    if (pathname != null) {
      val dot = pathname.lastIndexOf(".")
      if (dot != -1 && dot + 1 < pathname.length - 1)
        return pathname.substring(dot + 1)
    }
    ""
  }

}
