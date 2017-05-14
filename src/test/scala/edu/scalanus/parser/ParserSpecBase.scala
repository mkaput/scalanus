package edu.scalanus.parser

import java.io.{File, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import edu.scalanus.TestDataPathWithResult
import org.antlr.v4.runtime.tree.{ParseTree, Trees}
import org.antlr.v4.runtime.{CharStream, CharStreams, CommonTokenStream, Parser}
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

abstract class ParserSpecBase extends FunSuite with Matchers with TestDataPathWithResult {

  protected def createCommonTokenStream(stream: CharStream): CommonTokenStream

  protected def createParseTree(tokenStream: CommonTokenStream): (Parser, ParseTree)

  protected def lexIncludeHiddenTokens(f: File): Boolean = false

  private val files: Seq[File] = listFiles(testDataDir, Array("lex", EXT))

  files foreach (f => {
    val isLex = getExtension(f.getName) == "lex"

    val relativePath = f.getAbsolutePath.stripPrefix(testDataDir.getAbsolutePath).stripPrefix(File.separator)

    test(relativePath) {
      if (isLex) {
        doTestLex(f, relativePath)
      } else {
        doTestParser(f, relativePath)
      }
    }
  })

  private def doTestLex(f: File, relativePath: String) {
    val stream = CharStreams.fromPath(f.toPath)
    val tokens = createCommonTokenStream(stream)
    tokens.fill()

    val found = tokens.getTokens().asScala
      .withFilter(token => token.getChannel == 0 || lexIncludeHiddenTokens(f))
      .map(_.toString.trim)
      .mkString(System.lineSeparator)

    val expected = findExpected(relativePath, found)

    found shouldEqual expected
  }

  private def doTestParser(f: File, relativePath: String) {
    val stream = CharStreams.fromPath(f.toPath)
    val tokens = createCommonTokenStream(stream)
    val (parser, tree) = createParseTree(tokens)
    val found = Trees.toStringTree(tree, parser)
    val expected = findExpected(relativePath, found)
    found shouldEqual expected
  }

  private def findExpected(relativePath: String, found: String): String = {
    val resultPath = Paths.get(s"$resultDataPath/${stripExtension(relativePath)}.txt")
    val f = resultPath.toFile
    if (f.isFile) {
      Source.fromFile(f).mkString
    } else {
      val p = f.getParentFile
      if (!p.exists()) {
        if (!p.mkdirs()) {
          throw new IOException(s"Failed to create directory ${p.getAbsolutePath}")
        }
      } else if (p.isFile) {
        throw new IOException(s"Cannot make directory ${p.getAbsolutePath}")
      }

      Files.write(resultPath, found.getBytes())

      found
    }
  }

  private def listFiles(f: File, ext: Array[String]): Seq[File] = {
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
