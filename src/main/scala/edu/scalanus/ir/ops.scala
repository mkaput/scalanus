package edu.scalanus.ir

sealed abstract class IrOp(val rep: String) {
  override def toString: String = rep
}


//
// Unary ops
//

sealed abstract class IrUnaryOp(rep: String) extends IrOp(rep)

case object IrBNotOp extends IrUnaryOp("~")

case object IrNotOp extends IrUnaryOp("!")

case object IrMinusOp extends IrUnaryOp("-")

case object IrPlusOp extends IrUnaryOp("+")


//
// Increments/decrements
//

sealed abstract class IrIncrOp(rep: String) extends IrOp(rep)

case object IrPostfixDecrOp extends IrIncrOp("x--")

case object IrPostfixIncrOp extends IrIncrOp("x++")

case object IrPrefixDecrOp extends IrIncrOp("--x")

case object IrPrefixIncrOp extends IrIncrOp("++x")


//
// Binary ops
//

sealed abstract class IrBinaryOp(rep: String) extends IrOp(rep)

case object IrAddOp extends IrBinaryOp("+")

case object IrAndOp extends IrBinaryOp("and")

case object IrBandOp extends IrBinaryOp("&")

case object IrBitshiftLeftOp extends IrBinaryOp("<<")

case object IrBitshiftRightOp extends IrBinaryOp(">>")

case object IrBorOp extends IrBinaryOp("|")

case object IrDivOp extends IrBinaryOp("/")

case object IrEqOp extends IrBinaryOp("==")

case object IrGteqOp extends IrBinaryOp(">=")

case object IrGtOp extends IrBinaryOp(">")

case object IrLteqOp extends IrBinaryOp("<=")

case object IrLtOp extends IrBinaryOp("<")

case object IrModOp extends IrBinaryOp("mod")

case object IrMulOp extends IrBinaryOp("*")

case object IrNeqOp extends IrBinaryOp("!=")

case object IrOrOp extends IrBinaryOp("or")

case object IrPowOp extends IrBinaryOp("**")

case object IrSubOp extends IrBinaryOp("-")

case object IrXorOp extends IrBinaryOp("^")
