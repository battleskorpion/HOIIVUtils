package com.hoi4utils.clausewitz.data.country

import com.hoi4utils.clausewitz.script.{AbstractPDX, PDXScript, StringPDX}
import org.jetbrains.annotations.NotNull

import scala.collection.mutable.ListBuffer

object CountryTag extends Iterable[CountryTag] {
  val NULL_TAG = new CountryTag("###")
  val COUNTRY_TAG_LENGTH = 3 // standard country tag length (for a normal country tag)private final String tag;
  // scala... (this is null (????????????) if you dont use 'lazy')
  private lazy val _tagList: ListBuffer[CountryTag] = {
//    println("Initializing _tagList")
    ListBuffer[CountryTag]()
  }

  def get(tag: String): CountryTag = {
    for (countryTag <- _tagList) {
      if (countryTag.get().equals(tag)) return countryTag
    }
    NULL_TAG
  }

  override def iterator: Iterator[CountryTag] = {
    _tagList.iterator
  }
  
//  def tagList(): List[CountryTag] = _tagList
  def addTag(tag: CountryTag): Unit = {
    _tagList.addOne(tag)
  }
}

class CountryTag(tag: String) extends StringPDX(List("tag")) {
  setNode(tag)
  CountryTag.addTag(this)

  override def set(obj: String): Unit = {
    setNode(obj)
  }

  override def equals(other: PDXScript[?]): Boolean = {
    other match {
      case other: CountryTag =>
        super.equals(other)
      case _ => false
    }
  }

  override def toString: String = {
    get() match {
      case Some(tag) => tag
      case None => "[null country tag]"
    }
  }
}
