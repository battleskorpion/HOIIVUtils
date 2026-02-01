package com.hoi4utils.script.seq

import com.hoi4utils.parser.{Node, NodeSeq, SeqNode}
import com.hoi4utils.script.{PDXScript, VeryAbstractPDX}

import java.io.File

abstract class SeqPDX[T](protected var pdxIdentifiers: Seq[String]) extends VeryAbstractPDX[Seq[T], NodeSeq](pdxIdentifiers) with Seq[T]:
  
  /**
   * Updates the node tree for collection-based PDX scripts.
   * This method handles the common pattern of updating child nodes and rebuilding the parent node.
   *
   * @param items      Collection of PDXScript items to process
   * @param identifier Optional identifier for the parent node (defaults to pdxIdentifier)
   * @tparam U Type of PDXScript items in the collection
   */
  protected def updateCollectionNodeTree[U <: PDXScript[?, ?]](items: Iterable[U], identifier: String = pdxIdentifier): Unit =
    items.foreach(_.updateNodeTree())
    val childNodes: NodeSeq = items.flatMap(_.getNode).to(Seq)
    node match
      case Some(n) => n.setValue(childNodes)
      case None =>
        if (childNodes.nonEmpty)
          node = if (identifier != null && identifier.nonEmpty) Some(SeqNode(identifier, "=", childNodes))
          else Some(SeqNode(childNodes))
        else node = None

  /**
   * Template method for loading PDX collections with standardized error handling.
   * Subclasses should implement addToCollection to define collection-specific behavior.
   *
   * @param expression The node expression to load into the collection
   */
  protected def loadPDXCollection(expression: NodeType, file: Option[File]): Unit =
    try
      addToCollection(expression, file)
    catch
      case e: Exception =>
        handlePDXError(e, expression, file.orNull)
        node = Some(expression)

  /**
   * Abstract method for adding expressions to collections.
   * Must be implemented by collection-based PDX classes.
   *
   * @param expression The node expression to add to the collection
   */
  protected def addToCollection(expression: Node[?], file: Option[File]): Unit

  override def equals(other: PDXScript[?, ?]): Boolean =
    other match
      case pdx: SeqPDX[?] =>
        return false // TODO TODO
      case _ => false

