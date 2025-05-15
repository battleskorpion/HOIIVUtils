package com.hoi4utils.script

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Comment, Node}
import org.apache.poi.ss.formula.functions.T

import scala.collection.mutable.ListBuffer

abstract class StructuredPDX(pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[Node]](pdxIdentifiers) {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  var badNodesList: Iterable[Node] = ListBuffer.empty

  protected def childScripts: collection.mutable.Iterable[? <: PDXScript[?]]

  /**
   * Sets the current node to the provided expression and processes it based on its type.
   * If the expression is a ListBuffer of nodes, it will load each sub-PDXScript for processing.
   * Otherwise, an exception will be thrown indicating the mismatch in expected node value type.
   *
   * @param expression the node expression to set.
   * @throws UnexpectedIdentifierException if the identifier used in the expression is unexpected.
   * @throws NodeValueTypeException        if the expression is not of type ListBuffer[Node], but was expected to be.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match {
      case l: ListBuffer[Node] =>
        // then load each sub-PDXScript
        var list = Iterable.from(l)
        for (pdxScript <- childScripts) {
          list = pdxScript.loadPDX(list)
        }
        badNodesList = list
      case _ =>
        throw new NodeValueTypeException(expression, "list", this.getClass)
    }
  }

  override def set(value: ListBuffer[Node]): ListBuffer[Node] = {
    // todo?
    super.setNode(value)
    value
  }
  
  // 

  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty) {
      expression.$ match {
        case l: ListBuffer[Node] =>
          loadPDX(l)
        case _ =>
          System.out.println("Error loading PDX script: " + expression)
      }
    }
    else {
      try {
        set(expression)
      } catch {
        case e@(_: UnexpectedIdentifierException | _: NodeValueTypeException) =>
          System.out.println("Error loading PDX script: " + e.getMessage + "\n\t" + expression)
          // Preserve the original node in StructuredPDX as well.
          node = Some(expression)
      }
    }
  }

  override def loadPDX(expressions: Iterable[Node]): Iterable[Node] = {
    if (expressions != null) {
      val remaining = ListBuffer.from(expressions)
      expressions.filter(this.isValidIdentifier).foreach((expression: Node) => {
        try {
          loadPDX(expression)
          remaining -= expression
        }
        catch {
          case e: UnexpectedIdentifierException =>
            System.err.println(e.getMessage)
        }
      })
      remaining
    } else {
      ListBuffer.empty
    }
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXProperty(identifier: String): Option[PDXScript[?]] = {
    for (pdx <- childScripts) {
      if (pdx.pdxIdentifier == identifier) return Some(pdx)
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXProperty(identifiers: List[String]): Option[PDXScript[?]] = {
    for (identifier <- identifiers) {
      val pdx = getPDXProperty(identifier)
      if (pdx.isDefined) return pdx
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXPropertyOfType[R](identifier: String): Option[PDXScript[R]] = {
    for (pdx <- childScripts) {
      pdx match {
        case pdxScript: PDXScript[R] =>
          if (pdxScript.pdxIdentifier == identifier) return Some(pdxScript)
        case _ =>
      }
    }
    None
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXPropertyOfType[R](identifiers: List[String]): Option[PDXScript[R]] = {
    for (identifier <- identifiers) {
      val pdx = getPDXPropertyOfType[R](identifier)
      if (pdx.isDefined) return pdx
    }
    None
  }

  def pdxProperties: Iterable[? <: PDXScript[?]] = {
    val scripts = childScripts
    scripts match {
      case null => null
      case _ => scripts
    }
  }

  /**
   * Rebuilds the underlying Node tree on demand by gathering the child nodes from childScripts.
   * This ensures that any changes in the child PDXScript objects are reflected in the output.
   */
  override def updateNodeTree(): Unit = {
    // First, record the original positions of the nodes in the current node's value.
    val originalPositions: Map[String, Int] = node match {
      case Some(n) =>
        n.$ match {
          case lb: ListBuffer[Node] => lb.zipWithIndex.map { case (n, i) => n.identifier.getOrElse("") -> i }.toMap
          case _ => Map.empty
        }
      case None => Map.empty
    }

    // Update each child script's node tree.
    childScripts.foreach(_.updateNodeTree())

    // Get the loaded child nodes.
    val loadedChildNodes: ListBuffer[Node] = {
      childScripts.flatMap(_.getNodes).to(ListBuffer)
    }

    // Sort the loaded nodes based on their original positions.
    val sortedLoadedNodes = loadedChildNodes.sortBy { child =>
      child.identifier match {
        case Some(id) => originalPositions.getOrElse(id, Int.MaxValue)
        case None     => Int.MaxValue
      }
    }

//    // Retrieve the original nodes.
//    val originalNodes: ListBuffer[Node] = node match {
//      case Some(n) =>
//        n.$ match {
//          case lb: ListBuffer[Node] => lb
//          case _ => ListBuffer.empty[Node]
//        }
//      case None => ListBuffer.empty[Node]
//    }

//    // Preserve any original nodes that were not loaded by child scripts.
//    val preservedNodes = originalNodes.filterNot(orig =>
//      sortedLoadedNodes.exists(child => child.identifier == orig.identifier)
//    )
    val preservedNodes = badNodesList

    // Merge the loaded nodes and preserved nodes, then re-sort by the original order.
    val combinedNodes = (sortedLoadedNodes ++ preservedNodes)
      .sortBy(node => originalPositions.getOrElse(node.identifier.getOrElse(""), Int.MaxValue))


    // Update the current node's value.
    if (combinedNodes.nonEmpty) {
      node match {
        case Some(n) => n.setValue(combinedNodes)
        case None    => node = Some(new Node(pdxIdentifier, "=", combinedNodes))
      }
    } else {
      node = None
    }
  }

  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[StructuredPDX]
    clone.node = Some(Node(pdxIdentifier, "=", ListBuffer.empty))
    LOGGER.debug("Cloning StructuredPDX: {} -> {}", this, clone)
    clone.badNodesList = this.badNodesList
    clone
  }

}
