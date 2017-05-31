package edu.scalanus.ir

import edu.scalanus.util.LcfPosition

/** Root class in the IR class hierarchy.
  *
  * @param position Start position of the IR node in context of source file.
  *                 Used to generate stack traces and for debugging purposes.
  */
sealed abstract class IrNode(val position: LcfPosition) extends Product {

  def childrenNodes: Traversable[IrNode] =
    productIterator
      .flatMap {
        case collection: Array[IrNode] => collection
        case node: IrNode => Array(node)
        case _ => Nil
      }
      .toTraversable

  override def toString: String = s"${getClass.getSimpleName}($position)"

}

case class IrProgram(stmts: Array[_ <: IrStmt])(position: LcfPosition) extends IrNode(position)


//
// Statements
//

sealed trait IrStmt extends IrNode


//
// Expressions
//

sealed trait IrExpr extends IrNode with IrStmt

case class IrValue(value: Any)(position: LcfPosition) extends IrNode(position) with IrExpr
