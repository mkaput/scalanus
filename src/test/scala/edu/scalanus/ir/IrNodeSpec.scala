package edu.scalanus.ir

import edu.scalanus.util.LcfPosition
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class IrNodeSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  "IrNode" should "be comparable by children, but not by metadata" in {
    val node1 = IrValue(123)(LcfPosition(1, 2))
    val node2 = IrValue(123)(LcfPosition(3, 4))
    node1 shouldBe node2
  }

  it should "be not comparable between different node types" in {
    val node1 = IrValue(123)(LcfPosition(1, 2))
    val node2 = IrProgram(Array(node1))(LcfPosition(1, 2))
    node1 should not be node2
  }

  "IrNode.childrenNodes" should "return node arrays elements" in {
    val p = LcfPosition(0)
    val stmts = Array(IrValue(1)(p), IrValue(2)(p))
    val node = IrProgram(stmts)(p)
    node.childrenNodes should contain theSameElementsAs stmts
  }

  val tostrings = Table(
    ("node", "toString"),
    (IrProgram(Array[IrStmt]())(LcfPosition(1, 2)), "IrProgram(line 1:2)"),
    (IrValue(123)(LcfPosition(1, 2)), "IrValue(line 1:2) = 123"),
    (IrValue(123)(null), "IrValue(<unknown position>) = 123")
  )

  "IrNode.toString" should "produce human readable string including node class name and location" in {
    forEvery(tostrings) { (node, str) =>
      node.toString shouldBe str
    }
  }

}
