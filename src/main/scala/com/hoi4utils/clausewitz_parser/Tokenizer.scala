package com.hoi4utils.clausewitz_parser

import scala.collection.BufferedIterator
import scala.util.matching.Regex

class Tokenizer(@SuppressWarnings(Array("unused")) private val _input: String) {
  private val pattern: Regex = createPattern
  private val matcher: BufferedIterator[Regex.Match] = pattern.findAllMatchIn(_input).buffered
  //private var currentMatch: Option[Regex.Match] = None

  /**
   * Returns the next token from the tokenizer, or None if there are no more
   * tokens.
   *
   * @return the next token from the tokenizer, or None if there are no more tokens
   */
  def next: Option[Token] = {
    matcher.nextOption().map(m => Token(m.matched, m.start))
  }

  /**
   * Returns the next token from matcher without advancing the iterator.
   *
   * @return Next token from matcher
   */
  def peek: Option[Token] = {
    matcher.headOption.map(m => Token(m.matched, m.start))
  }

  /**
   * Creates a pattern that matches the tokens in the text.
   *
   * @return a pattern that matches the tokens in the text
   */
  private def createPattern = {
    val patternBuilder = new StringBuilder
    for (tokenType <- TokenType.values) {
      val tokenRegex = Token.tokenRegex.get(tokenType)
      tokenRegex match {
        case Some(value) => patternBuilder.append(s"|(?<$tokenType>${value.pattern})")
        case None =>
      }
    }
    if (patternBuilder.nonEmpty) new Regex(patternBuilder.substring(1)) // Skip the leading "|"
    else throw new IllegalStateException("No patterns found for token types.")
  }

}
