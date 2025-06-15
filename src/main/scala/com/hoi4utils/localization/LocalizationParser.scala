package com.hoi4utils.localization


object LocalizationParser:

  /** Parses a line of localization text and returns an Option[Localization].
   * The line should be in the format: "key: [number] "quoted text""
   * If the line is not properly formatted, it returns None.
   * If the line is valid, it returns Some(Localization) with the parsed values.
   *
   * @param line the line to parse
   * @return Some(Localization) if the line is valid, None otherwise
   * @throws IllegalArgumentException if the quoted string is not properly terminated
   */
  @throws[IllegalArgumentException]
  def parseLine(line: String): Option[Localization] =
    val colonIndex = line.indexOf(':')
    if colonIndex < 0 then
      None
    else
      val key = line.substring(0, colonIndex).trim
      val afterColon = line.substring(colonIndex + 1).trim

      // Split out the number (if any)
      val (numPart, restAfterNumber) = afterColon.span(_.isDigit)
      val ver = if numPart.isEmpty then null else numPart.toIntOption
      val afterNumber = restAfterNumber.trim

      // Check that we start with a quote
      if !afterNumber.startsWith("\"") then
        None
      else
        val (quotedText, indexAfterQuote) = parseQuoted(afterNumber, 0)
        val trailing = afterNumber.substring(indexAfterQuote).trim
        Some(Localization(key, ver, quotedText, Localization.Status.EXISTS))

  /** Parses a quoted string starting at startIndex (which should point to a double quote).
   * It handles inner escaped quotes represented by a double double-quote.
   * Returns a tuple with the parsed string and the index immediately after the closing quote.
   * @param s the string to parse
   * @param startIndex the index where the quoted string starts (should be the index of the opening quote)
   * @return a tuple containing the parsed string and the index after the closing quote
   * @throws IllegalArgumentException if the quoted string is not properly terminated
   */
  @throws[IllegalArgumentException]
  private def parseQuoted(s: String, startIndex: Int): (String, Int) =
    // We expect s(startIndex) to be the starting quote.
    val sb = new StringBuilder
    var i = startIndex + 1 // Skip the initial quote
    while i < s.length do
      if s(i) == '"' then
        // If the next char is also a quote, it is an escaped quote.
        if i + 1 < s.length && s(i + 1) == '"' then
          sb.append('"')
          i += 2
        else
          // End of quoted text.
          return (sb.toString, i + 1)
      else
        sb.append(s(i))
        i += 1
    throw new IllegalArgumentException(s"Unterminated quoted string in: $s\n    at index: $startIndex")