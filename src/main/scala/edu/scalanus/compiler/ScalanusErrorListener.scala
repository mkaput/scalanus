package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusException, ScalanusMultiException}

import scala.collection.mutable.ArrayBuffer

trait ScalanusErrorListener {

  private val errors: ArrayBuffer[ScalanusException] = ArrayBuffer()

  def hasErrors: Boolean =
    errors.nonEmpty

  @throws[ScalanusException]
  def validate(): Unit = {
    if (errors.nonEmpty) {
      throw new ScalanusMultiException(errors: _*)
    }
  }

  def report(exception: ScalanusException): Unit =
    errors += exception

}

object ScalanusErrorListener {
  def apply(): ScalanusErrorListener = new ScalanusErrorListener {}
}
