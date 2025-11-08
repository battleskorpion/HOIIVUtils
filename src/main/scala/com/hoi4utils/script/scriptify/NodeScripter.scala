package com.hoi4utils.script.scriptify

import com.hoi4utils.parser.{Node, Token}

trait NodeScripter {

  def toScript(node: Node): String = toScriptAccumulator(node, "")
    /**
     * Reconstructs the original file text by concatenating:
     * - leading trivia (whitespace/comments)
     * - the identifier and operator tokens
     * - the node's value (or nested children, if a block)
     * - trailing trivia
     */
    protected def toScriptAccumulator(node: Node, indent: String): String =
      given sb: StringBuilder = new StringBuilder

      appendTrivia(node.leadingTrivia, indent)
      appendPrefix(node, indent)
      appendRhs(node, indent)

      sb.toString()

  protected def appendRhs(using sb: StringBuilder)(node: Node, str: String): Unit

  /**
   * Append the identifier and operator (if any)
   * @param sb String builder to append to
   * @param node
   */
  protected def appendPrefix(using sb: StringBuilder)(node: Node, indent: String): Unit =
    node.identifier.foreach { id =>
      sb.append(indent).append(id)
      node.operator.foreach(op => sb.append(" ").append(op))
      sb.append(" ") // separate id/op from the value
    }

  /**
   *
   * @param sb String builder to append to
   * @param children
   * @return
   */
  protected def appendNumberBlock(using sb: StringBuilder)(children: Iterable[Node]) =
    sb.append("{ ")
    sb.append(children.map(_.asString).mkString(" "))
    sb.append(" }").append('\n')

  /**
   *
   * @param sb String builder to append to
   * @param node
   * @param indent
   * @param children
   * @return
   */
  protected def appendChildrenBlock(using sb: StringBuilder)
                                   (node: Node, indent: String, children: Iterable[Node]): Unit =
    val hasHeader = node.identifier.nonEmpty
    // For a block of child nodes, open a brace and newline
    appendChildScriptOpening(hasHeader)
    // append children
    val childIndent = if hasHeader then indent + "\t" else indent
    appendChildren(children, childIndent)
    appendChildScriptClosing(indent, hasHeader)

  /**
   *
   * @param sb String builder to append to
   * @param hasHeader
   * @return
   */
  protected def appendChildScriptOpening(using sb: StringBuilder)(hasHeader: Boolean): Unit =
    if hasHeader then sb.append("{\n")

  /**
   *
   * @param sb String builder to append to
   * @param indent
   * @param hasHeader
   * @return
   */
  protected def appendChildScriptClosing(using sb: StringBuilder)(indent: String, hasHeader: Boolean): Unit =
    if hasHeader then
      // Remove all trailing whitespace from the StringBuilder
      while sb.nonEmpty && sb.charAt(sb.length - 1).isWhitespace do
        sb.deleteCharAt(sb.length - 1)
      sb.append('\n').append(indent).append("}")
    else sb.append('\n')

  /**
   * yes
   * @param sb String builder to append to
   * @param children
   * @param indent
   */
  protected def appendChildren(using sb: StringBuilder)(children: Iterable[Node], indent: String): Unit

  /**
   * Appends trivia on its own line(s) (preserving comments/whitespace)
   *
   * @param sb String builder to append to
   * @param trivia
   * @param indent
   */
  protected def appendTrivia(using sb: StringBuilder)(trivia: Iterable[Token], indent: String): Unit =
    for t <- trivia do sb.append(indent).append(t.value.replaceAll("\\t+", ""))

  protected def isNumberBlock(children: Iterable[Node]): Boolean =
    children.forall(n =>
      n.identifier.isEmpty &&
        n.operator.isEmpty &&
        n.rawValue.exists(_.isInstanceOf[Int | Double])
    )

}
