package com.hoi4utils.clausewitz.data.country

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
      if (countryTag.get.equals(tag)) return countryTag
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

class CountryTag(val tag: String) extends Comparable[CountryTag] {
  CountryTag.addTag(this)
  
  def get: String = tag
  
  override def toString: String = {
    tag
  }
  
  override def equals(obj: Any): Boolean = {
    obj match {
      case other: CountryTag =>
        tag.equals(other.tag)
      case _ => false
    }
  }

  override def compareTo(o: CountryTag): Int = {
    tag.compareTo(o.tag)
  }
}
