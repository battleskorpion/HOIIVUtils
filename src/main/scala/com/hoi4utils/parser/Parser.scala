package com.hoi4utils.parser

import java.io.File
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

object Parser {
  val escape_backslash_regex = "\\\\"
  val escape_quote_regex = "\\\\\""
}

class Parser {
  private var tokens: Tokenizer = uninitialized
  private var _rootNode: Node = uninitialized

  def this(input: String) = {
    this()
    // Append EOF indicator so tokenizer can mark the end.
    val str = input.concat(Token.EOF_INDICATOR)
    tokens = new Tokenizer(str)
  }

  def this(file: File) = {
    this(new String(Files.readAllBytes(file.toPath)))
  }

  /**
   * Consumes and returns any tokens that are “trivia” (e.g., whitespace, comments).
   */
  private def consumeTrivia(): ListBuffer[Token] = {
    val trivia = ListBuffer[Token]()
    while (tokens.peek.exists(t => t.`type` == TokenType.whitespace || t.`type` == TokenType.comment || t.value.matches("^[,;]"))) {
      trivia += tokens.next.get
    }
    trivia
  }

  @throws[ParserException]
  def parse: Node = {
    // Capture any leading trivia for the whole file.
    val leading = consumeTrivia()

    // Parse the block content that forms the root node.
    val blockContent = parseBlockContent()
    if (blockContent.isEmpty)
      throw new ParserException("Parsed block content was empty")
    // Consume any trailing trivia.
    val trailing = consumeTrivia()
    tokens.peek match {
      case Some(token) =>
        token.`type` match {
          case TokenType.eof => // OK
          case _ => throw new ParserException("Input not completely parsed. Last token: " + token.value)
        }
      case None => throw new ParserException("Input not completely parsed. Last token: null")
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
      consumeTrivia()
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

    val idToken = tokens.next.getOrElse(
      throw new ParserException("Unexpected end of input while parsing node identifier")
    )

    // If the token is a comment, create a node that holds it.
    if (idToken.`type` == TokenType.comment) {
      return new Node(
        leadingTrivia = leading,
        identifierToken = Some(idToken),
        rawValue = Some(new Comment(idToken.value)),
        trailingTrivia = consumeTrivia()
      )
    }

    // Ensure the token is a valid identifier.
    if (idToken.`type` != TokenType.string && idToken.`type` != TokenType.symbol && !idToken.isNumber)
      throw new ParserException("Parser: incorrect token type " + idToken.`type` + " at index " + idToken.start + " for: " + idToken.value)

    // Consume any trivia after the identifier.
    consumeTrivia()
    var nextToken = tokens.peek.getOrElse(
      throw new ParserException("Unexpected end of input after identifier")
    )
    var operatorOpt: Option[Token] = None
    var raw: Option[String | Int | Double | Boolean | ListBuffer[Node] | Comment] = None

    if (nextToken.`type` != TokenType.operator || nextToken.value.matches("^[,;}]$")) {
      /*
      example where you would make it inside here:
        color = { 1.0 1.0 1.0 }
        colortwo = { 1.0 1.0 1.0 }
      each 1.0 will be a node which makes it within here
       */
      while (nextToken.value.matches("^[,;]$")) {
        tokens.next
        nextToken = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token"))
      }
      // No proper operator: treat this node as a value-only node.
      raw = Some(parseThisTokenValue(idToken))
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
      identifierToken = Some(idToken),
      operatorToken = operatorOpt,
      rawValue = raw,
      trailingTrivia = trailing
    )
  }

  @throws[ParserException]
  def parseNodeValue(): String | Int | Double | Boolean | ListBuffer[Node] | Comment = {
    // Consume any trivia before the value.
    consumeTrivia()
    val nextToken = tokens.next.getOrElse(
      throw new ParserException("Unexpected end of input while parsing node value")
    )
    nextToken.`type` match {
      case TokenType.string =>
        if (nextToken.value.length > 2)
          try {
            nextToken.value.substring(1, nextToken.value.length - 1)
              .replaceAll(Parser.escape_quote_regex, "\"")
              .replaceAll(Parser.escape_backslash_regex, java.util.regex.Matcher.quoteReplacement("\\"))
          } catch {
            case _: IllegalArgumentException =>
              println("Illegal argument exception while parsing string: " + nextToken.value)
              nextToken.value
          }
        else nextToken.value
      case TokenType.float =>
        nextToken.value.toDouble
      case TokenType.int =>
        if (nextToken.value.startsWith("0x"))
          Integer.parseInt(nextToken.value.substring(2), 16)
        else nextToken.value.toInt
      case TokenType.symbol =>
        nextToken.value
      case TokenType.operator if nextToken.value == "{" =>
        // If the operator indicates a block, parse the block content.
        val children = parseBlockContent()
        val closing = tokens.next.getOrElse(
          throw new ParserException("Expected closing '}'")
        )
        if (closing.value != "}")
          throw new ParserException("Expected closing '}', got " + closing.value)
        children
      case _ =>
        throw new ParserException("Unexpected token type in node value: " + nextToken.`type`)
    }
  }

  @throws[ParserException]
  def parseThisTokenValue(token: Token): String | Int | Double | Boolean | ListBuffer[Node] | Comment = {
    token.`type` match {
      case TokenType.string =>
        if (token.value.length > 2)
          token.value.substring(1, token.value.length - 1)
            .replaceAll(Parser.escape_quote_regex, "\"")
            .replaceAll(Parser.escape_backslash_regex, "\\")
        else token.value
      case TokenType.float =>
        token.value.toDouble
      case TokenType.int =>
        if (token.value.startsWith("0x"))
          Integer.parseInt(token.value.substring(2), 16)
        else token.value.toInt
      case TokenType.symbol =>
        token.value
      case _ =>
        throw new ParserException("Unexpected token type: " + token.`type`)
    }
  }

  def rootNode: Node = _rootNode
}
