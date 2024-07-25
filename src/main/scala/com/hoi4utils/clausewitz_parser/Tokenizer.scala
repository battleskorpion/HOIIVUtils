package com.hoi4utils.clausewitz_parser

import java.util.regex.Matcher
import java.util.regex.Pattern

class Tokenizer(@SuppressWarnings(Array("unused")) private val input: String) {
  final private var pattern: Pattern = createPattern
  final private var matcher: Matcher = this.pattern.matcher(input)
  private var findStartIndex = 0
  private var peekOccurred = false

  /**
   * Returns the next token from the tokenizer, or null if there are no more
   * tokens.
   *
   * @return the next token from the tokenizer, or null if there are no more
   *         tokens
   */
  def next: Token = try {
    /*
           * matcher.find(int) resets the matcher, therefore,
           * only use this call when necessary
           */
    if (peekOccurred) if (matcher.find(findStartIndex)) {
      val value = matcher.group(0)
      val start = matcher.start
      peekOccurred = false
      return new Token(value, start)
    }
    else if (matcher.find) {
      val value = matcher.group(0)
      val start = matcher.start
      /* peek occurred already false */
      return new Token(value, start)
    }
    null
  } catch {
    case e: StackOverflowError =>
      System.err.println("EEEE") // todo

      System.err.println(matcher.group)
      null
  }

  /**
   * <code> Matcher </code> has no peek() function therefore this logic is
   * necessary.
   * returns the next token from matcher, the next <code> find() </code> call to
   * <code> matcher </code> however will be from the same start index.
   *
   * @see Matcher
   * @return Next token from matcher
   */
  def peek: Token = {
    if (peekOccurred) if (matcher.find(findStartIndex)) {
      val value = matcher.group
      val start = matcher.start
      /* find_start_index remains the same */
      /* peek occurred already true */
      return new Token(value, start)
    }
    else if (matcher.find) {
      val value = matcher.group
      val start = matcher.start
      findStartIndex = matcher.start
      peekOccurred = true
      return new Token(value, start)
    }
    null
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
      if (tokenRegex != null) patternBuilder.append(String.format("|(?<%s>%s)", tokenType, tokenRegex.pattern))
    }
    if (patternBuilder.nonEmpty) Pattern.compile(patternBuilder.substring(1)) // Skip the leading "|"
    else throw new IllegalStateException("No patterns found for token types.")
  }
}
