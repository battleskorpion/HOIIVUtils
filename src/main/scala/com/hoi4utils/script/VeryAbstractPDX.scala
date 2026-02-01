package com.hoi4utils.script

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.{Node, NodeValueType, ParsingContext}
import com.hoi4utils.script.{AbstractPDX, PDXFileError, PDXScript}

import java.io.File
import scala.collection.mutable.ListBuffer

trait VeryAbstractPDX[T, NodeValue <: NodeValueType](protected var pdxIdentifiers: Seq[String])
  extends PDXScript[T, NodeValue]:

  private[script] var activeIdentifier = 0

  /**
   * Sets the active identifier to match the given expression,
   * if it is a valid identifier. Otherwise, throws exception.
   *
   * @param expr the expression to check, and set the active identifier to the identifier of the expression
   * @throws UnexpectedIdentifierException if the expression is not a valid identifier
   */
  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(expr: Node[?]): Unit =
    if (pdxIdentifiers.nonEmpty)
      val index = pdxIdentifiers.indexWhere(expr.nameEquals)
      if index == -1 then throw new UnexpectedIdentifierException(expr)

      activeIdentifier = index
    else
      // identifiers empty: assume to be purposeful and to mean *no* identifier by default
      expr.identifier match
        case Some(_) => throw new UnexpectedIdentifierException(expr, "Expected no identifier")
        case None => ()

  def value: Option[T]

  /**
   *
   * @param expressions pdx node that is iterable
   * @return remaining unloaded expressions
   */
  override def loadPDX(expressions: SeqNodeType): SeqNodeType =
    expressions match
      case null => Seq.empty
      case _ =>
        val remaining = ListBuffer.from(expressions) // TODO fp
        expressions filter isValidIdentifier foreach { expression =>
          loadPDX(expression, None)
          remaining -= expression
        }
        remaining.toSeq
      
  def handlePDXError(exception: Exception = null, node: Node[?] = null, file: File = null): Unit =
    given ParsingContext = if node != null then new ParsingContext(file, node) else ParsingContext(file)

    if exception.getClass == classOf[UnsupportedOperationException] then throw exception
    val pdxError = new PDXFileError(
      exception = exception,
      errorNode = node,
      pdxScript = this,
    )
    logger.error(pdxError.toString)
    exception.printStackTrace()
