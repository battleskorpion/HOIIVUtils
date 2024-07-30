package com.hoi4utils.clausewitz.data.country

import com.hoi4utils.clausewitz.script.AbstractPDX
import com.hoi4utils.clausewitz.script.PDXScript
import org.jetbrains.annotations.NotNull

import scala.collection.mutable.ListBuffer

object CountryTag extends Iterable[CountryTag] {
  val NULL_TAG = new CountryTag("###")
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  private[country] val _tagList = new ListBuffer[CountryTag]

  def get(tag: String): CountryTag = {
    for (countryTag <- _tagList) {
      if (countryTag.get().equals(tag)) return countryTag
    }
    NULL_TAG
  }

  override def iterator: Iterator[CountryTag] = {
    _tagList.iterator
  }
}

final class CountryTag(tag: String) extends AbstractPDX[String](List("tag")) with Comparable[CountryTag] {
  set(tag)
  CountryTag._tagList.addOne(this)

  override def compareTo(o: CountryTag): Int = this.get().compareTo(o.get())

  override def nodeEquals(other: PDXScript[_]): Boolean = {
    if (other.isInstanceOf[CountryTag]) return this.nodeEquals(pdx)
    false
  }
}
