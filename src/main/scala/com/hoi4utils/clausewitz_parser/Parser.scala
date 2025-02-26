package com.hoi4utils.clausewitz_parser

import java.io.{File, IOException}
import java.nio.file.Files
import scala.collection.mutable.ListBuffer

/*
 * Parser File new
 */
object Parser {
  val escape_backslash_regex = "\\\\"
  val escape_quote_regex = "\\\\\""
}

class Parser {
  final private var tokens: Tokenizer = _
  private var rootNode: Node = _

  def this(input: String) = {
    this()

    /* EOF */
    val str = input.concat(Token.EOF_INDICATOR) 
    tokens = new Tokenizer(str)
  }

  def this(file: File) = {
    this({
      new String(Files.readAllBytes(file.toPath))
    })
  }

  @throws[ParserException]
  def parse: Node = {
    val value = parseBlockContent(tokens)
    if (value.isEmpty) {
      throw new ParserException("parsed block content was empty")
    }

    /*
     * need to reach up to eof indicator
     * if last token is '}' this could indicate there was a
     * missing '{' in the code
     */
    tokens.peek match {
      case Some(token) =>
        token.`type` match {
          case TokenType.eof => // good
          case _ => throw new ParserException("Input not completely parsed by clausewitz-file parser \n" + "\t\tlast token: " + token.value)
        }
      case None => throw new ParserException("Input not completely parsed by clausewitz-file parser \n" + "\t\tlast token: null")
    }
    rootNode = new Node(value)
    rootNode
  }

  @throws[ParserException]
  def parseBlockContent(tokens: Tokenizer): ListBuffer[Node] = {
    val nodes = new ListBuffer[Node]

    var cont = true
    while (cont) {
      val nextToken: Token = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token"))
      if ((nextToken.`type` eq TokenType.eof) || nextToken.value == "}") {
        cont = false
      } else {
        try {
          val n = parseNode(tokens)
          // check if n is a comment node
          if (n.nameToken.`type` ne TokenType.comment) nodes.addOne(n)
        } catch {
          case e: ParserException =>
            throw e
        }
      }
    }
    nodes
  }

  @throws[ParserException]
  def parseNode(tokens: Tokenizer): Node = {
    val name = tokens.next.getOrElse(throw new ParserException("Unexpected null next token"))
    /* skip comments */
    if (name.`type` eq TokenType.comment) {
      val node = new Node
      node.identifier = name.value
      node.nameToken = name
      return node
    }
    // System.out.println(name.value);
    if ((name.`type` ne TokenType.string) && (name.`type` ne TokenType.symbol) && (name.`type` ne TokenType.number)) throw new ParserException("Parser: incorrect token type " + name.`type` + ", token: " + name + " at index: " + name.start)
    var nextToken = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token"))
    if ((nextToken.`type` ne TokenType.operator) || nextToken.value.matches("^[,;}]$")) {
      while (nextToken.value.matches("^[,;]$")) {
        tokens.next
        nextToken = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token"))
      }
      /* handle escaped characters */
      val nameValue = unescapeCharacters(name, name.value)
      // TODO node value?
      val node = new Node
      node.identifier = nameValue
      node.nameToken = name
      node.operator = null
      node.operatorToken = null
      /* node.value = null; */
      return node
    }

    /* "= {" */
    var operator: Token = null
    if (nextToken.value == "{") {
      operator = new Token("=", nextToken.start, TokenType.operator) // Create a new Token with the '=' value

      nextToken = new Token("=", operator.start, TokenType.operator)
      nextToken.start = operator.start
      operator.value = "="
    }
    else operator = tokens.next.get

    var parsedValue = parseNodeValue(tokens)
    /* Handle value attachment (e.g., when there's a nested block) */
    if (parsedValue != null && parsedValue.getValue.isInstanceOf[Node]) {
      val peekedToken = tokens.peek.get
      if (peekedToken.value == "{") {
        parsedValue = parseNodeValue(tokens)
      }
    }
    // Skip comments before tailComma
    var tailComma = tokens.peek.get
    while ((tailComma.`type` eq TokenType.comment) || tailComma.value.matches("^[,;]")) {
      tokens.next
      tailComma = tokens.peek.getOrElse(throw new ParserException("Unexpected null next token"))
    }
    // todo
    val node = new Node(parsedValue)
    node.identifier = name.value
    node.nameToken = name
    node.operator = operator.value
    node.operatorToken = operator
    // node.value = parsedValue;
    // node.valueStartToken = (Token) parsedValue[1];
    // node.valueEndToken = (Token) parsedValue[2];
    node
  }

  private def unescapeCharacters(name: Token, nameValue: String): String = {
    if (name.`type` eq TokenType.string) nameValue
      .substring(1, nameValue.length - 2)
      .replaceAll(Parser.escape_quote_regex, "\"")
      .replaceAll(Parser.escape_backslash_regex, "\\")
    else nameValue
  }

  @throws[ParserException]
  def parseNodeValue(tokens: Tokenizer): NodeValue = {
    val nextToken = tokens.next.getOrElse(return null)
    // todo eeeh?
    nextToken.`type` match {
      case TokenType.string =>
        if (nextToken.length == 1) System.out.println("Parser: ?? " + nextToken.value)
        /* substring from 1 to length() - 2: don't replace "" */
        if (nextToken.value.length == 2) return new NodeValue(nextToken.value)
        return new NodeValue(nextToken.value.substring(1, nextToken.length - 2).replaceAll(Parser.escape_quote_regex, "\"").replaceAll(Parser.escape_backslash_regex, "\\"))

      case TokenType.number =>
        /* handles hexadecimal or floating-point/integers */
        return new NodeValue(if (nextToken.value.startsWith("0x")) Integer.parseInt(nextToken.value.substring(2), 16)
        else nextToken.value.toDouble)

      case TokenType.symbol => return new NodeValue(nextToken.value)

      case TokenType.operator => if (nextToken.value == "{") {
        val result = parseBlockContent(tokens)
        val right = tokens.next.getOrElse(throw new ParserException("Parser expected a matching \"}\""))
        if (!(right.value == "}")) throw new ParserException("Parser expected a matching \"}\"")
        return new NodeValue(result)
      }
      // necessary if addtl. case added, so will keep.


      case _ => throw new ParserException("Unexpected value: " + nextToken.`type`)
    }
    throw new ParserException("Parser expected a string, number, symbol, or {")
  }

  def getRootNode: Node = this.rootNode
}
