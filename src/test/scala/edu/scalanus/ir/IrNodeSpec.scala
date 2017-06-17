package edu.scalanus.ir

import edu.scalanus.util.LcfPosition
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class IrNodeSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  def mock(f: => (IrCtx) => IrNode): IrNode = mockCfg(LcfPosition(1, 2))(f)

  def mockCfg(position: LcfPosition)(f: => (IrCtx) => IrNode): IrNode = f(IrCtx(position))

  "IrNode" should "be comparable by children, but not by metadata" in {
    val node1 = mock(IrValue(123))
    val node2 = mockCfg(LcfPosition(3, 4))(IrValue(123))
    node1 shouldBe node2
  }

  it should "be not comparable between different node types" in {
    val node1 = mock(IrValue(123))
    val node2 = mock(IrProgram(Array(node1)))
    node1 should not be node2
  }

  "IrNode.childrenNodes" should "return node arrays elements" in {
    val stmts = Array(mock(IrValue(1)), mock(IrValue(2)))
    val node = mock(IrProgram(stmts))
    node.childrenNodes should contain theSameElementsAs stmts
  }

  val tostrings = Table(
    ("node", "toString"),
    (mock(IrProgram(Array[IrStmt]())), "IrProgram(line 1:2)"),
    (mock(IrValue(123)), "IrValue(line 1:2) = 123"),
    (mockCfg(position = null)(IrValue(123)), "IrValue(<unknown position>) = 123")
  )

  "IrNode.toString" should "produce human readable string including node class name and location" in {
    forEvery(tostrings) { (node, str) =>
      node.toString shouldBe str
    }
  }

}
