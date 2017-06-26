package edu.scalanus.interpreter

import javax.script.SimpleBindings

sealed abstract class ScalanusScope extends SimpleBindings

class SoftScope() extends ScalanusScope

class HardScope() extends ScalanusScope
