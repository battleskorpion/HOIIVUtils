package com.hoi4utils.parser

import scalafx.beans.value
import zio.{UIO, URIO, ZIO}

import java.io.File
import java.lang.AutoCloseable
import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class ZIOParser(pdx: String | File = null) {
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

  def parse: ZIO[Any, ParserException, Node] =
    for {
      _ <- ZIO.succeed(System.err.println(s"[DEBUG] ZIOParser.parse starting for $pdx"))
      // Capture any leading trivia for the whole file.
      leading <- consumeTrivia()
      _ <- ZIO.succeed(System.err.println(s"[DEBUG] ZIOParser.parse consumed leading trivia for $pdx"))
      blockContent <- parseBlockContent()
      _ <- ZIO.succeed(System.err.println(s"[DEBUG] ZIOParser.parse parsed block content for $pdx, nodes: ${blockContent.size}"))
      _ <- ZIO.cond(blockContent.nonEmpty, (), ParserException("Parsed block content was empty"))
      trailing <- consumeTrivia()
      _ <- tokens.peek match
        case Some(token) if token.`type` == TokenType.eof => ZIO.unit
        case Some(token) => ZIO.fail(ParserException("Input not completely parsed", token))
        case None => ZIO.fail(ParserException("Input not completely parsed - no tokens remaining"))
      root = new Node(
        leadingTrivia = leading,
        rawValue = Some(blockContent),
        trailingTrivia = trailing
      )
      _ <- ZIO.succeed {
        _rootNode = root
      }
    } yield root

  def parseBlockContent(): ZIO[Any, ParserException, List[Node]] =
    def loop(acc: List[Node] = Nil, prevPos: Int = -1): ZIO[Any, ParserException, List[Node]] =
      for {
        _ <- consumeTrivia()
        result <- tokens.peek match
          // base case
          case Some(token) if token.`type` == TokenType.eof || token.value == "}" =>
            ZIO.succeed(acc.reverse) // Reverse at end
          // recursive case
          case Some(token) =>
            val pos = token.start
            if pos == prevPos then ZIO.fail(ParserException("Parser stuck at token", token))
            else
              for {
                node <- parseNode()
                next <- loop(node :: acc, pos)
              } yield next
          case None => ZIO.fail(ParserException("Finished without required EOF token. An error occurred."))
      } yield result

    loop()

  def parseNode(): ZIO[Any, ParserException, Node] =
    for {
      leading <- consumeTrivia()
      tokenIdentifier <- ZIO.fromOption(tokens.next).orElseFail(ParserException("Unexpected end of input while parsing node identifier"))

      node <-
        if (tokenIdentifier.`type` == TokenType.comment)
          // If the token is a comment, create a node that holds it.
          consumeTrivia().map { trailing =>
            new Node(
              leadingTrivia = leading,
              identifierToken = Some(tokenIdentifier),
              rawValue = Some(new Comment(tokenIdentifier.value)),
              trailingTrivia = trailing
            )
          }
        else
          // parse node
          for {
            _ <- validateIdentifier(tokenIdentifier)
            // Consume any trivia after the identifier.
            _ <- consumeTrivia()

            peekedToken <- ZIO.fromOption(tokens.peek)
              .orElseFail(ParserException("Unexpected end of input after identifier", tokenIdentifier))

            nodeResult <-
              if (peekedToken.`type` != TokenType.operator || peekedToken.value.matches("^[,;}]$"))
                handleValueOnlyNode(tokenIdentifier, leading)
              else
                handleOperatorNode(tokenIdentifier, leading)
          } yield nodeResult
    } yield node

  def parseNodeValue(): ZIO[Any, ParserException, NodeValueType] =
    for {
      _ <- consumeTrivia()

      token <- ZIO.fromOption(tokens.next)
        .orElseFail(ParserException("Unexpected end of input while parsing node value"))
      value = token.value

      result <- token.`type` match
        case TokenType.string => ZIO.succeed(parseStringValue(value))
        case TokenType.float => ZIO.succeed(value.toDouble) // todo parse
        case TokenType.int => ZIO.succeed(parseIntValue(value))
        case TokenType.symbol => ZIO.succeed(value)
        case TokenType.operator if value == "{" => parseEnclosedBlockContent("}")
        case _ => ZIO.fail(ParserException("Unexpected token type in node value", token))
    } yield result

  def parseThisTokenValue(token: Token): ZIO[Any, ParserException, NodeValueType] =
    val value = token.value
    for {
      result <- token.`type` match
        case TokenType.string => ZIO.succeed(parseStringValue(value))
        case TokenType.float => ZIO.succeed(value.toDouble) // todo parse
        case TokenType.int => ZIO.succeed(parseIntValue(value))
        case TokenType.symbol => ZIO.succeed(value)
        case _ => ZIO.fail(ParserException("Unexpected token type", token))
    } yield result

  /**
   * Parses block content that has been identified as a block enclosed by defined opening and closing operators.
   * Ensures the parsed block content is closed with the expected closing operator.
   *
   * @param closingOp
   * @return
   * @throws ParserException if the parsed block content is not closed with the expected closing operator, or a syntax issue leads to this state.
   */
  private def parseEnclosedBlockContent(terminator: String): ZIO[Any, ParserException, Seq[Node]] =
    for {
      content <- parseBlockContent()
      closingToken <- ZIO.fromOption(tokens.next).orElseFail(ParserException(s"Expected block terminator '$terminator'"))
      _ <- ZIO.cond(closingToken.value == terminator, (), ParserException(s"Expected closing '$terminator'", closingToken))
    } yield content

  private def parseIntValue(value: String) =
    if (value.startsWith("0x")) Integer.parseInt(value.substring(2), 16)
    else value.toInt

  private def parseStringValue(value: String) =
    if (value.length > 2)
      value.substring(1, value.length - 1)
        .replaceAll(escape_quote_regex, "\"")
        .replaceAll(escape_backslash_regex, java.util.regex.Matcher.quoteReplacement("\\"))
    else value

  /**
   * Consumes and returns any tokens that are “trivia” (e.g., whitespace, comments).
   */
  private def consumeTrivia(): UIO[Seq[Token]] = ZIO.suspendSucceed {
    tokens.peek match
      case Some(t) if isTrivia(t) =>
        ZIO.succeed(tokens.next) *> consumeTrivia().map(t +: _)
      case _ => ZIO.succeed(Nil)
  }

  /**
   * Returns if a token should be considered a 'trivia' token (does not affect the CST layout/program semantics).
   * This method still must be called in the correct locations as certain token types such as [[TokenType.whitespace]] may or may not be crucial depending on the context.
   * @param t Token
   * @return <code>true</code> if the token may be trivia.
   */
  private def isTrivia(t: Token): Boolean =
    t.`type` == TokenType.whitespace || t.`type` == TokenType.comment ||
      (t.value.length == 1 && (t.value == "," || t.value == ";"))

  def rootNode: Node = _rootNode

  ////////////////////////////////////////////////////////////////////////////////////////////

  // Helper: Validation logic
  private def validateIdentifier(idToken: Token): ZIO[Any, ParserException, Unit] =
    if (idToken.`type` == TokenType.string || idToken.`type` == TokenType.symbol || idToken.isNumber) ZIO.unit
    else ZIO.fail(ParserException("Incorrect token type for identifier", idToken))

  // Helper: Logic for value-only nodes (like inside color blocks)
  private def handleValueOnlyNode(id: Token, leading: Seq[Token]): ZIO[Any, ParserException, Node] =
    for {
      _         <- consumeSeparators()
      raw       <- parseThisTokenValue(id)
      trailing  <- consumeTrivia()
    } yield new Node(
      leadingTrivia = leading,
      rawValue = Some(raw),
      trailingTrivia = trailing
    )

  // Helper: Logic for standard key = value nodes
  private def handleOperatorNode(id: Token, leading: Seq[Token]): ZIO[Any, ParserException, Node] =
    for {
      opToken <- ZIO.succeed(tokens.next.get)
      // Consume any trivia after the operator.
      _ <- consumeTrivia()
      raw <- parseNodeValue()
      trailing <- consumeTrivia()
    } yield new Node(
      leadingTrivia = leading,
      identifierToken = Some(id),
      operatorToken = Some(opToken),
      rawValue = Some(raw),
      trailingTrivia = trailing
    )

  // Helper: Skip separators
  private def consumeSeparators(): ZIO[Any, Nothing, Unit] =
    ZIO.succeed {
      while (tokens.peek.exists(_.value.matches("^[,;]$"))) {
        tokens.next
      }
    }
}
