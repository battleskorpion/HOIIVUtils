package com.hoi4utils.parser

import com.hoi4utils.parser.Parser.parserFileErrors

import java.io.File
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class Parser(val input: String | File, clazz: Class[?]):
  private val escapeBackslashRegex = "\\\\"
  private val escapeQuoteRegex = "\\\\\""

  private val tokens: Tokenizer = input match
    case str: String =>
      Tokenizer(str.concat(Token.EOF_INDICATOR))
    case file: File =>
      Tokenizer(String(Files.readAllBytes(file.toPath)).concat(Token.EOF_INDICATOR))

  /**
   * Parses the file and constructs a root node containing all nodes
   *
   * @return The root node with all of its child nodes
   */
  private val _rootNode: Node =
    val leading = consumeTrivia()
    val blockContent = parseBlockContent()
    if blockContent.isEmpty then parserFileErrors.addOne(s"Class: ${clazz.getSimpleName} \n File: ${input.toString} \n Error: No nodes found in the block.")
    val trailing = consumeTrivia()

    tokens.peek match
      case Some(token) if token.tokenType == TokenType.eof => // OK
      case Some(token) => parserFileErrors.addOne(s"Class: ${clazz.getSimpleName} \n File: ${input.toString} \n Error: Unexpected token after block parsing: ${token.value}")
      case None => parserFileErrors.addOne(s"Class: ${clazz.getSimpleName} \n File: ${input.toString} \n Error: No tokens found after parsing block.")

    Node(
      leadingTrivia = leading,
      rawValue = Some(blockContent),
      trailingTrivia = trailing
    )

  def rootNode: Node = _rootNode

  /** Consumes and returns any tokens that are "trivia" (e.g., whitespace, comments). */
  private def consumeTrivia(): ListBuffer[Token] = {
    val trivia = ListBuffer[Token]()
    while (tokens.peek.exists(t => t.tokenType == TokenType.whitespace || t.tokenType == TokenType.comment || t.value.matches("^[,;]"))) {
      trivia += tokens.next.get
    }
    trivia
  }

  /**
   * Parses the content of a block, which is expected to be enclosed in curly braces `{}`.
   * This method collects nodes until it encounters a closing brace or EOF.
   *
   * @return A ListBuffer containing the parsed nodes within the block.
   */
  private def parseBlockContent(): ListBuffer[Node] =
    val nodes = ListBuffer[Node]()

    @annotation.tailrec
    def collectNodes(): Unit =
      consumeTrivia()
      tokens.peek match
        case Some(token) if token.tokenType == TokenType.eof || token.value == "}" => // Stop collecting
        case _ =>
          try
            nodes += parseNode()
          catch
            case e: ParserException =>
              parserFileErrors.addOne(s"Class: ${clazz.getSimpleName} \n File: ${input.toString} \n Error: ${e.getMessage}")
              return // Stop collecting on error
          collectNodes()

    collectNodes()
    nodes

  /**
   * Parses a single node from the input tokens.
   * @throws ParserException if the node identifier is invalid or if the input ends unexpectedly.
   * @return A Node object representing the parsed node.
   */
  @throws[ParserException]
  private def parseNode(): Node =
    val leading = consumeTrivia()

    val idToken = tokens.next.getOrElse(
      throw new ParserException("Unexpected end of input while parsing node identifier")
    )

    // Handle comment tokens
    if idToken.tokenType == TokenType.comment then
      return Node(
        leadingTrivia = leading,
        identifierToken = Some(idToken),
        rawValue = Some(Comment(idToken.value)),
        trailingTrivia = consumeTrivia()
      )

    // Validate identifier token
    if !(idToken.tokenType == TokenType.string ||
      idToken.tokenType == TokenType.symbol ||
      idToken.isNumber) then
      throw ParserException(
        clazz,
        s"Parser: incorrect token type ${idToken.tokenType} at index ${idToken.start} for: ${idToken.value}",
        input.toString
      )

    consumeTrivia()

    val nextToken = tokens.peek.getOrElse {
      throw ParserException(clazz, "Unexpected end of input after identifier", input.toString)
    }

    // Handle nodes without proper operators
    if nextToken.tokenType != TokenType.operator || nextToken.value.matches("^[,;}]$") then
      // Skip comma/semicolon separators
      @annotation.tailrec
      def skipSeparators(): Unit =
        tokens.peek match
          case Some(token) if token.value.matches("^[,;]$") =>
            tokens.next
            skipSeparators()
          case _ => // done

      skipSeparators()

      Node(
        leadingTrivia = leading,
        rawValue = Some(parseTokenValue(idToken)),
        trailingTrivia = consumeTrivia()
      )
    else
      // Handle nodes with operators
      val operator = Some(tokens.next.get)
      consumeTrivia()
      val value = Some(parseNodeValue())
      val trailing = consumeTrivia()

      Node(
        leadingTrivia = leading,
        identifierToken = Some(idToken),
        operatorToken = operator,
        rawValue = value,
        trailingTrivia = trailing
      )

  @throws[ParserException]
  private def parseNodeValue(): String | Int | Double | Boolean | ListBuffer[Node] | Comment =
    consumeTrivia()

    val nextToken = tokens.next.getOrElse {
      throw ParserException(clazz, "Unexpected end of input while parsing node value", input.toString)
    }

    parseTokenValue(nextToken) match
      case blockContent: ListBuffer[Node] => blockContent
      case other => other

  @throws[ParserException]
  private def parseTokenValue(token: Token): String | Int | Double | Boolean | ListBuffer[Node] | Comment =
    token.tokenType match
      case TokenType.string =>
        parseStringToken(token)
      case TokenType.float =>
        token.value.toDouble
      case TokenType.int =>
        parseIntToken(token)
      case TokenType.symbol =>
        token.value
      case TokenType.operator if token.value == "{" =>
        parseBlock()
      case _ =>
        throw ParserException(clazz, s"Unexpected token type: ${token.tokenType}", input.toString)

  private def parseStringToken(token: Token): String =
    if token.value.length <= 2 then
      token.value
    else
      Try {
        token.value.substring(1, token.value.length - 1)
          .replaceAll(escapeQuoteRegex, "\"")
          .replaceAll(escapeBackslashRegex, java.util.regex.Matcher.quoteReplacement("\\"))
      } match
        case Success(result) => result
        case Failure(_) => token.value

  private def parseIntToken(token: Token): Int =
    if token.value.startsWith("0x") then
      Integer.parseInt(token.value.substring(2), 16)
    else
      token.value.toInt

  @throws[ParserException]
  private def parseBlock(): ListBuffer[Node] =
    val children = parseBlockContent()
    val closing = tokens.next.getOrElse {
      throw ParserException(clazz, "Expected closing '}'", input.toString)
    }
    if closing.value != "}" then
      parserFileErrors.addOne(s"Class: ${clazz.getSimpleName} \n File: ${input.toString} \n Error: Unexpected end of block type: \n        Required: }, \n        Found: ${closing.value}")
    children

object Parser:
  val parserFileErrors: ListBuffer[String] = ListBuffer.empty