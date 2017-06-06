package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusException, ScalanusMultiException}

import scala.collection.mutable.ArrayBuffer

trait ErrorListenerBase[T <: ScalanusException] {

  private val errors: ArrayBuffer[T] = ArrayBuffer()

  def hasErrors: Boolean =
    errors.nonEmpty

  @throws[ScalanusException]
  def validate(): Unit = {
    if (errors.nonEmpty) {
      throw ScalanusMultiException(errors: _*)
    }
  }

  def report(exception: T): Unit =
    errors += exception

}
