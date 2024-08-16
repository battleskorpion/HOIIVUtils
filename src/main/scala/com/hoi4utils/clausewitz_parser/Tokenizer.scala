package com.hoi4utils.clausewitz_parser

import scala.util.matching.Regex
import scala.collection.BufferedIterator

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
      //if (tokenRegex != null) patternBuilder.append(String.format("|(?<%s>%s)", tokenType, tokenRegex.pattern))
      tokenRegex match {
        case Some(value) => patternBuilder.append(s"|(?<${tokenType}>${value.pattern})")
        case None =>
      }
    }
    if (patternBuilder.nonEmpty) new Regex(patternBuilder.substring(1)) // Skip the leading "|"
    else throw new IllegalStateException("No patterns found for token types.")
  }

//  final private var pattern: Pattern = createPattern
//  final private var matcher: Matcher = this.pattern.matcher(input)
//  private var findStartIndex = 0
//  private var peekOccurred = false
//
//  /**
//   * Returns the next token from the tokenizer, or null if there are no more
//   * tokens.
//   *
//   * @return the next token from the tokenizer, or null if there are no more
//   *         tokens
//   */
//  def next: Token = try {
//    /*
//     * matcher.find(int) resets the matcher, therefore,
//     * only use this call when necessary
//     */
//    if (peekOccurred) if (matcher.find(findStartIndex)) {
//      val value = matcher.group(0)
//      val start = matcher.start
//      peekOccurred = false
//      return new Token(value, start)
//    }
//    else if (matcher.find) {
//      val value = matcher.group(0)
//      val start = matcher.start
//      /* peek occurred already false */
//      return new Token(value, start)
//    }
//    null
//  } catch {
//    case e: StackOverflowError =>
//      System.err.println("EEEE") // todo
//
//      System.err.println(matcher.group)
//      null
//  }
//
//  /**
//   * <code> Matcher </code> has no peek() function therefore this logic is
//   * necessary.
//   * returns the next token from matcher, the next <code> find() </code> call to
//   * <code> matcher </code> however will be from the same start index.
//   *
//   * @see Matcher
//   * @return Next token from matcher
//   */
//  def peek: Token = {
//    if (peekOccurred) if (matcher.find(findStartIndex)) {
//      val value = matcher.group
//      val start = matcher.start
//      /* find_start_index remains the same */
//      /* peek occurred already true */
//      return new Token(value, start)
//    }
//    else if (matcher.find) {
//      val value = matcher.group
//      val start = matcher.start
//      findStartIndex = matcher.start
//      peekOccurred = true
//      return new Token(value, start)
//    }
//    null
//  }

}
