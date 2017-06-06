package edu.scalanus.ir

import edu.scalanus.util.LcfPosition

/** Root class in the IR class hierarchy.
  *
  * @param position Start position of the IR node in context of source file.
  *                 Used to generate stack traces and for debugging purposes.
  *                 Can be null.
  */
sealed abstract class IrNode(val position: LcfPosition) extends Product {

  def childrenNodes: Traversable[IrNode] =
    productIterator
      .flatMap {
        // FIXME this generates compiler warning
        case collection: IndexedSeq[IrNode] => collection
        case node: IrNode => Array(node)
        case _ => Nil
      }
      .toTraversable

  override def toString: String =
    s"${getClass.getSimpleName}(${if (position != null) position else "<unknown position>"})"

}


case class IrProgram(stmts: IndexedSeq[IrNode])(position: LcfPosition) extends IrNode(position)


//
// Statements
//

sealed trait IrStmt extends IrNode


//
// Expressions
//

sealed trait IrExpr extends IrNode with IrStmt

case class IrValue(value: Any)(position: LcfPosition) extends IrNode(position) with IrExpr
