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

  tokens = new Tokenizer(pdx match
    // EOF indicator is appended so tokenizer can mark the end.
    case pdx: File => new String(Files.readAllBytes(pdx.toPath)).concat(Token.EOF_INDICATOR)
    case pdx: String => pdx.concat(Token.EOF_INDICATOR)
  )

  /**
   * Consumes and returns any tokens that are “trivia” (e.g., whitespace, comments).
   */
  private def consumeTrivia(): ListBuffer[Token] = {
    val trivia = ListBuffer[Token]()
    while (tokens.peek.exists(t =>
      t.`type` == TokenType.whitespace ||
        t.`type` == TokenType.comment ||
        (t.value.length == 1 && (t.value == "," || t.value == ";"))
    )) {
      trivia += tokens.next.get
    }
    trivia
  }

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

    val tokenIdentifier = tokens.next match
      case Some(token) => token
      case None =>
        throw ParserException("Unexpected end of input while parsing node identifier")

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
    val nextToken = tokens.next.getOrElse(
      throw new ParserException("Unexpected end of input while parsing node value")
    )
    nextToken.`type` match {
      case TokenType.string =>
        if (nextToken.value.length > 2)
          try {
            nextToken.value.substring(1, nextToken.value.length - 1)
              .replaceAll(escape_quote_regex, "\"")
              .replaceAll(escape_backslash_regex, java.util.regex.Matcher.quoteReplacement("\\"))
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
  def parseThisTokenValue(token: Token): NodeValueType = {
    token.`type` match {
      case TokenType.string =>
        if (token.value.length > 2)
          token.value.substring(1, token.value.length - 1)
            .replaceAll(escape_quote_regex, "\"")
            .replaceAll(escape_backslash_regex, "\\")
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
