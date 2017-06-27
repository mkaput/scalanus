package edu.scalanus.interpreter

import javax.script.SimpleBindings

sealed abstract class ScalanusScope extends SimpleBindings

case class SoftScope() extends ScalanusScope

case class HardScope() extends ScalanusScope
