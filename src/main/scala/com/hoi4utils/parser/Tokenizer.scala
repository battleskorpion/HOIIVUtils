package com.hoi4utils.parser

import scala.collection.BufferedIterator
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

class Tokenizer(@SuppressWarnings(Array("unused")) private val _input: String) {
  private val pattern: Regex = createPattern
  private val matcher: BufferedIterator[Regex.Match] = pattern.findAllMatchIn(_input).buffered

  private var peekedToken: Option[Token] = None

  // Build line offset table once - O(n) where n = input length
  private val lineOffsets: Array[Int] =
    val charsWithIndices = _input.toCharArray.zipWithIndex        // don't have to check if each index is safe for charAt(i) (screw ur memory :))
    val offsets = ArrayBuffer(0)                      // Line 1 starts at position 0
    offsets.sizeHint(charsWithIndices.length / 30)    // scripting files usually small and have some blank lines too

    offsets ++= (charsWithIndices filter ((char, index) => char == '\n') map (_._2 + 1))  // +1 since next line starts after newline
    offsets.toArray

  /**
   * Calculates line and column number for a given character position.
   * Uses binary search on pre-built line offset table - O(log n) where n = number of lines
   *
   * @param position Character position in the input string
   * @return Tuple of (line number, column number), both 1-indexed
   */
  private def getLineAndColumn(position: Int): (Int, Int) =
    // Binary search to find which line this position is on
    var left = 0
    var right = lineOffsets.length - 1
    var lineIndex = 0

    while (left <= right)
      val mid = (left + right) / 2
      if (lineOffsets(mid) <= position)
        lineIndex = mid
        left = mid + 1
      else
        right = mid - 1

    val line = lineIndex + 1
    val column = position - lineOffsets(lineIndex) + 1
    (line, column)

  /**
   * Returns the next token from the tokenizer, or None if there are no more
   * tokens.
   *
   * @return the next token from the tokenizer, or None if there are no more tokens
   */
  @inline def next: Option[Token] = peekedToken match
    case Some(token) =>
      matcher.next()  // iterate matcher
      val nextToken = peekedToken
      peekedToken = None
      nextToken
    case None =>
      createToken(() => matcher.nextOption())

  /**
   * Returns the next token from matcher without advancing the iterator.
   *
   * @return Next token from matcher
   */
  @inline def peek: Option[Token] =
    if peekedToken.isEmpty then
      peekedToken = peekNextToken()
    peekedToken

  private def peekNextToken(): Option[Token] =
    createToken(() => matcher.headOption)

  private def createToken(matchSupplier: () => Option[Regex.Match]): Option[Token] =
    matchSupplier().map { m =>
      val (line, column) = getLineAndColumn(m.start)
      new Token(m.matched, m.start, line, column)
    }

  /**
   * Creates a pattern that matches the tokens in the text.
   *
   * @return a pattern that matches the tokens in the text
   */
  private def createPattern =
    val patternBuilder = new StringBuilder
    for (
      tokenType <- TokenType.values
    ) do
      val tokenRegex = Token.tokenRegex.get(tokenType)
      tokenRegex match
        case Some(value) => patternBuilder.append(s"|(?<$tokenType>${value.pattern})")
        case None =>
    if (patternBuilder.nonEmpty) new Regex(patternBuilder.substring(1)) // Skip the leading "|"
    else throw new IllegalStateException("No patterns found for token types.")
}
