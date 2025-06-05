package com.hoi4utils

implicit class RichString(val s: String) extends AnyVal {
  def splitWithDelimiters(regex: String, limit: Int = 0): Array[String] = {
    val pattern = regex.r
    val matches = pattern.findAllMatchIn(s).toList

    if (matches.isEmpty) return Array(s)

    val result = scala.collection.mutable.ArrayBuffer[String]()
    var lastEnd = 0

    for (m <- matches) {
      if (m.start > lastEnd) {
        result += s.substring(lastEnd, m.start) // before match
      }
      result += m.matched // the delimiter itself
      lastEnd = m.end
    }

    if (lastEnd < s.length) {
      result += s.substring(lastEnd) // remainder
    }

    if (limit > 0) result.take(limit).toArray else result.toArray
  }
}

