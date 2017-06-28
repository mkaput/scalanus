package edu.scalanus.interpreter

abstract sealed class ScalanusControlFlowException() extends Exception

case class ScalanusReturn() extends ScalanusControlFlowException

case class ScalanusBreak() extends ScalanusControlFlowException

case class ScalanusContinue() extends ScalanusControlFlowException
