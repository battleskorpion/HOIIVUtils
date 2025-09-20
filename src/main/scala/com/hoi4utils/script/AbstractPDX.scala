package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, Parser, ParserException}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * <p>
 */
trait AbstractPDX[T](protected var pdxIdentifiers: List[String]) extends PDXScript[T] {
  private[script] var activeIdentifier = 0
  protected[script] var node: Option[Node] = None

  /**
   * Sets the active identifier to match the given expression, 
   * if it is a valid identifier. Otherwise, throws exception. 
   * @param expr the expression to check, and set the active identifier to the identifier of the expression
   * @throws UnexpectedIdentifierException if the expression is not a valid identifier
   */
  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(expr: Node): Unit = {
    if (pdxIdentifiers.isEmpty) {
      // all good? 
    }
    else if (pdxIdentifiers.indexWhere(expr.nameEquals) == -1) {
      throw new UnexpectedIdentifierException(expr)
    } 
  }

  /**
   * @inheritdoc
   */
  override protected def setNode(value: T | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit = {
    if (node.isEmpty) {
      return
    }
    if (value == null) {
      setNull()
      return
    }
    value match {
      case s: String => node.get.setValue(s)
      case i: Int => node.get.setValue(i)
      case d: Double => node.get.setValue(d)
      case b: Boolean => node.get.setValue(b)
      case l: ListBuffer[Node] => node.get.setValue(l)
      case _ => throw new RuntimeException(s"Unsupported type: ${value.getClass}")
    }
  }

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    setNode(expression.$)
  }

  /**
   * @inheritdoc
   */
  override def value: Option[T] = {
    node.getOrElse(return None).$ match {
      case value: T => Some(value)
      case _ => None
    }
  }

  /**
   * @inheritdoc
   */
  override def getNode: Option[Node] = node

  override def getNodes: List[Node] = getNode match {
    case Some(node) => List(node)
    case None => List.empty
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: Node): Unit = {
    if (expression.identifier.isEmpty && (pdxIdentifiers.nonEmpty || expression.isEmpty)) {
      logger.error("Error loading PDX script: " + expression)
      return
    }
    try set(expression)
    catch
      case e: UnexpectedIdentifierException =>
        handleUnexpectedIdentifier(expression, e)
        // Preserve the original node in StructuredPDX as well.
        node = Some(expression)
      case e: NodeValueTypeException        =>
        handleNodeValueTypeError(expression, e)
        // Preserve the original node in StructuredPDX as well.
        node = Some(expression)
  }

  /**
   * 
   * @param expressions
   * @return remaining unloaded expressions
   */
  def loadPDX(expressions: Iterable[Node]): Iterable[Node] = expressions match
    case null => ListBuffer.empty
    case _ =>
      val remaining = ListBuffer.from(expressions)
      expressions.filter(this.isValidIdentifier).foreach(expression =>
        loadPDX(expression)
        remaining -= expression
      )
      remaining


  protected def loadPDX(file: File): Unit = {
    require(file.exists && file.isFile, s"File $file does not exist or is not a file.")
    val pdxParser = new Parser(file)
    try loadPDX(pdxParser.parse)
    catch case e: ParserException => handleParserException(file, e)
  }

  /**
   * @inheritdoc
   */
  override def isValidIdentifier(node: Node): Boolean = {
    isValidID(node.name)
  }
  
  override def isValidID(identifier: String): Boolean = {
    pdxIdentifiers.contains(identifier)
  }

  /**
   * @inheritdoc
   */
  override def clearNode(): Unit = {
    node = None
  }

  /**
   * @inheritdoc
   */
  override def setNull(): Unit = {
    node.foreach(_.setNull())
  }

  override def loadOrElse(exp: Node, value: T): Unit = {
    try loadPDX(exp)
    catch {
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
    if (node.get.valueIsNull) set(value)
  }

  /**
   * Rebuilds the underlying Node tree from the current state.
   * For simple leaf nodes, this is a no-op.
   * Composite types (e.g. StructuredPDX) should override this method to rebuild their Node tree.
   */
  override def updateNodeTree(): Unit = {
    // Default behavior for leaf nodes: update the node's value from the current state.
    node.foreach(n => setNode(value.orNull))
  }

  /**
   * Generates the script output.
   * Before returning the script, updateNodeTree() is called so that the underlying Node reflects any changes.
   */
  override def toScript: String = {
    updateNodeTree()
    node.map(_.toScript).getOrElse("")
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case pdx: AbstractPDX[?] =>
        if (node == null) return false
        if (pdx.node == null) return false
        node.equals(pdx.node)
      case _ => false
    }
  }

  /**
   * @inheritdoc
   */
  override def getOrElse(elseValue: T): T = {
    val value = node.getOrElse(return elseValue).value
    value match
      case Some(t: T) => t
      case _ => elseValue
  }

  override def toString: String = {
    if (node.isEmpty || node.get.isEmpty) {
      if (value.isEmpty) {
        return super.toString
      } else {
        return value.get.toString
      }
    }
    node.toString
  }

  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull

  override def isDefined: Boolean = !isUndefined

  override def pdxIdentifier: String = {
    if (pdxIdentifiers.isEmpty) return null
    else pdxIdentifiers(activeIdentifier)
  }

  /**
   * Checks whether the node’s value is an instance of the specified class.
   */
  def valueIsInstanceOf[A](implicit ct: ClassTag[A]): Boolean = {
    node.exists(n => ct.runtimeClass.isInstance(n.$))
  }

  def getPDXTypeName: String = {
    pdxIdentifier
  }

  def schema(): PDXSchema[T] = {
    var schema = new PDXSchema[T](pdxIdentifiers*)
    null.asInstanceOf[PDXSchema[T]]  // todo no
  }

  def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"PDX Type: ${Option(this.pdxIdentifier).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Expected Identifiers: ${if (pdxIdentifiers.nonEmpty) pdxIdentifiers.mkString("[", ", ", "]") else "any"}"
    errorDetails += s"Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Node Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}"
    errorDetails += s"Active Identifier Index: $activeIdentifier"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    logger.error("Unexpected Identifier Error:")
    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  def handleNodeValueTypeError(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"PDX Type: ${Option(this.pdxIdentifier).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Expected Type: T (generic parameter)"
    errorDetails += s"Actual Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Actual Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}"
    errorDetails += s"Node Has Value: ${node.$ != null}"
    errorDetails += s"Node Is Empty: ${node.isEmpty}"
    errorDetails += s"Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    logger.error("Node Value Type Error:")
    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  def handleParserException(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"PDX Type: ${Option(this.pdxIdentifier).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Node Content: ${if (node.isEmpty) "empty" else "has content"}"
    errorDetails += s"Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Parser Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    logger.error("Parser Exception (Node):")
    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  def handleParserException(file: File, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"PDX Type: ${Option(this.pdxIdentifier).getOrElse("undefined")}"
    errorDetails += s"File Path: ${file.getAbsolutePath}"
    errorDetails += s"File Name: ${file.getName}"
    errorDetails += s"File Exists: ${file.exists()}"
    errorDetails += s"File Size: ${if (file.exists()) s"${file.length()} bytes" else "N/A"}"
    errorDetails += s"File Readable: ${file.canRead}"
    errorDetails += s"Parser Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    logger.error("Parser Exception (File):")
    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  // Optional: Generic error handler that can be used by subclasses for custom errors
  protected def handleGenericError(errorType: String, context: Map[String, Any], exception: Option[Exception] = None): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"PDX Type: ${Option(this.pdxIdentifier).getOrElse("undefined")}"

    // Add context information
    context.foreach { case (key, value) =>
      errorDetails += s"$key: ${Option(value).map(_.toString).getOrElse("null")}"
    }

    // Add exception details if provided
    exception.foreach { ex =>
      errorDetails += s"Exception Type: ${ex.getClass.getSimpleName}"
      errorDetails += s"Exception Message: ${ex.getMessage}"
      if (ex.getCause != null) {
        errorDetails += s"Root Cause: ${ex.getCause.getMessage}"
      }
    }

    logger.error(s"$errorType Error:")
    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }
}