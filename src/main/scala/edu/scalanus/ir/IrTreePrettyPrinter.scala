package edu.scalanus.ir

object IrTreePrettyPrinter {
  private val INDENT = "    "

  def apply(root: IrNode): String = {
    val sb = new StringBuilder
    apply(root, 0, sb)
    sb.toString()
  }

  private def apply(node: IrNode, indent: Int, sb: StringBuilder): Unit = {
    sb.append(INDENT * indent).append(node).append('\n')
    node.childrenNodes.foreach {
      apply(_, indent + 1, sb)
    }
  }
}
