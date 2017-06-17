package edu.scalanus.ir

import edu.scalanus.util.LcfPosition

/** Represents IR node context information
  *
  * @param position Start position of the IR node in context of source file.
  *                 Used to generate stack traces and for debugging purposes.
  *                 Can be null.
  */
sealed case class IrCtx(position: LcfPosition) {
  override def toString: String = if (position != null) position.toString else "<unknown position>"
}

/** Root class in the IR class hierarchy */
sealed abstract class IrNode(val ctx: IrCtx) extends Product {

  def childrenNodes: Traversable[IrNode] =
    productIterator
      .flatMap {
        // FIXME this generates compiler warning
        case collection: IndexedSeq[IrNode] => collection
        case node: IrNode => Array(node)
        case _ => Nil
      }
      .toTraversable

  override def toString: String = s"${getClass.getSimpleName}($ctx)"

}


case class IrProgram(stmts: IndexedSeq[IrNode])(ctx: IrCtx) extends IrNode(ctx)


//
// Statements
//

sealed trait IrStmt extends IrNode


//
// Expressions
//

sealed trait IrExpr extends IrNode with IrStmt

case class IrValue(value: Any)(ctx: IrCtx) extends IrNode(ctx) with IrExpr {
  override def toString: String = s"${super.toString} = $value"
}
