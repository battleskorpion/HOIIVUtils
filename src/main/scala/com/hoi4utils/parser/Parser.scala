package com.hoi4utils.parser

import java.io.File
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class Parser(pdx: String | File = null) {
  private val escape_backslash_regex = "\\\\"
  private val escape_quote_regex = "\\\\\""
  private var tokens: Tokenizer = uninitialized
  private var _rootNode: Node = uninitialized

  /* for ParserExceptions, would have been a lot to pass every time */
  given currentPdx: File | String = pdx

  tokens = new Tokenizer(
    s"${
      pdx match
        case f: File => Files.readString(f.toPath)
        case s: String => s
    }${System.lineSeparator()}${Token.EOF_INDICATOR}"
  )

  /**
   * Consumes and returns any tokens that are “trivia” (e.g., whitespace, comments).
   */
  private def consumeTrivia(): ListBuffer[Token] = {
    val trivia = ListBuffer.empty[Token]
    while tokens.peek.exists(isTrivia) do
      trivia += tokens.next.get
    trivia
  }

  private def isTrivia(t: Token): Boolean =
    t.`type` == TokenType.whitespace || t.`type` == TokenType.comment ||
      (t.value.length == 1 && (t.value == "," || t.value == ";"))


  @throws[ParserException]
  def parse: Node = {
    // Capture any leading trivia for the whole file.
    val leading = consumeTrivia()

    // Parse the block content that forms the root node.
    val blockContent = parseBlockContent()  // todo fix
    if (blockContent.isEmpty)
      throw ParserException("Parsed block content was empty")
    // Consume any trailing trivia.
    val trailing = consumeTrivia()
    tokens.peek match {
      case Some(token) =>
        token.`type` match {
          case TokenType.eof => // OK
          case _ => throw ParserException("Input not completely parsed. Last token: " + token.value)
        }
      case None => throw ParserException(s"Input not completely parsed. Tokens: ${tokens}")
    }
    // Create the root node using the parsed children (a ListBuffer[Node]) as its raw value.
    _rootNode = new Node(
      leadingTrivia = leading,
      rawValue = Some(blockContent),
      trailingTrivia = trailing
    )
    _rootNode
  }

  @throws[ParserException]
  def parseBlockContent(): ListBuffer[Node] = {
    val nodes = ListBuffer[Node]()
    var cont = true
    while (cont) {
      // Consume any trivia before each node.
      val t = consumeTrivia()
      tokens.peek match {
        case Some(token) if token.`type` == TokenType.eof || token.value == "}" =>
          cont = false
        case _ =>
          val node = parseNode()
          nodes += node
      }
    }
    nodes
  }

  @throws[ParserException]
  def parseNode(): Node = {
    // Capture leading trivia for this node.
    val leading = consumeTrivia()

    val tokenIdentifier = tokens.next match
      case Some(token) => token
      case None => throw ParserException("Unexpected end of input while parsing node identifier")

    // If the token is a comment, create a node that holds it.
    if (tokenIdentifier.`type` == TokenType.comment) {
      return new Node(
        leadingTrivia = leading,
        identifierToken = Some(tokenIdentifier),
        rawValue = Some(new Comment(tokenIdentifier.value)),
        trailingTrivia = consumeTrivia()
      )
    }

    // Ensure the token is a valid identifier.
    if (tokenIdentifier.`type` != TokenType.string && tokenIdentifier.`type` != TokenType.symbol && !tokenIdentifier.isNumber)
      throw ParserException("Parser: incorrect token type " + tokenIdentifier.`type` + " at index " + tokenIdentifier.start + " for: " + tokenIdentifier.value)

    // Consume any trivia after the identifier.
    consumeTrivia()
    var nextToken = tokens.peek.getOrElse(
      throw ParserException("Unexpected end of input after identifier\nFound token: " + tokenIdentifier.value)
    )
    var operatorOpt: Option[Token] = None
    var raw: Option[NodeValueType] = None

    if (nextToken.`type` != TokenType.operator || nextToken.value.matches("^[,;}]$")) {
      /*
      example where you would make it inside here:
        color = { 1.0 1.0 1.0 }
        colortwo = { 1.0 1.0 1.0 }
      each 1.0 will be a node which makes it within here
       */
      while (nextToken.value.matches("^[,;]$")) {
        tokens.next
        nextToken = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token\nFound token: " + tokenIdentifier.value))
      }
      // No proper operator: treat this node as a value-only node.
      raw = Some(parseThisTokenValue(tokenIdentifier))
      return new Node(
        leadingTrivia = leading,
        rawValue = raw,
        trailingTrivia = consumeTrivia()
      )
    } else {
      // Consume the operator token.
      operatorOpt = Some(tokens.next.get)
      // Consume any trivia after the operator.
      consumeTrivia()
      // Parse the node’s value.
      raw = Some(parseNodeValue())
    }
    // Consume trailing trivia.
    val trailing = consumeTrivia()
    // Create and return the new node with all CST information attached.
    new Node(
      leadingTrivia = leading,
      identifierToken = Some(tokenIdentifier),
      operatorToken = operatorOpt,
      rawValue = raw,
      trailingTrivia = trailing
    )
  }

  @throws[ParserException]
  def parseNodeValue(): NodeValueType = {
    // Consume any trivia before the value.
    consumeTrivia()

    val token = tokens.next.getOrElse(
      throw new ParserException("Unexpected end of input while parsing node value")
    )
    val value = token.value

    token.`type` match
      case TokenType.string => parseStringValue(value)
      case TokenType.float => value.toDouble
      case TokenType.int => parseIntValue(value)
      case TokenType.symbol => value
      case TokenType.operator if value == "{" => parseEnclosedBlockContent("}")
      case _ => throw new ParserException("Unexpected token type in node value: " + token.`type`)
  }

  private def parseEnclosedBlockContent(closingOp: String) = {
    // If the operator indicates a block, parse the block content.
    val children = parseBlockContent()
    val closing = tokens.next.getOrElse(
      throw new ParserException(s"Expected closing '$closingOp'")
    )
    if (closing.value != closingOp)
      throw new ParserException(s"Expected closing '$closingOp', got " + closing.value)
    children
  }

  private def parseIntValue(value: String) = {
    if (value.startsWith("0x"))
      Integer.parseInt(value.substring(2), 16)
    else value.toInt
  }

  private def parseStringValue(value: String) = {
    if (value.length > 2)
      try {
        value.substring(1, value.length - 1)
          .replaceAll(escape_quote_regex, "\"")
          .replaceAll(escape_backslash_regex, java.util.regex.Matcher.quoteReplacement("\\"))
      } catch {
        case _: IllegalArgumentException =>
          println("Illegal argument exception while parsing string: " + value)
          value
      }
    else value
  }

  @throws[ParserException]
  def parseThisTokenValue(token: Token): NodeValueType = {
    val value = token.value
    token.`type` match {
      case TokenType.string => parseStringValue(value)
      case TokenType.float => value.toDouble
      case TokenType.int => parseIntValue(value)
      case TokenType.symbol => value
      case _ => throw new ParserException("Unexpected token type: " + token.`type`)
    }
  }

  def rootNode: Node = _rootNode
}
