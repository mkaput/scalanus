package edu.scalanus.ir

import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext

/** Represents IR node context information
  *
  * @param position Start position of the IR node in context of source file.
  *                 Used to generate stack traces and for debugging purposes.
  *                 Can be null.
  */
case class IrCtx(position: LcfPosition) {

  override def toString: String =
    if (position != null) position.toString else "<unknown position>"

}

object IrCtx {

  def apply(ctx: ParserRuleContext): IrCtx = IrCtx(
    position = LcfPosition(ctx)
  )

}
