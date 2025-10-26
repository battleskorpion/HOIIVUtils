package com.hoi4utils.hoi4.map.gen

import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.history.countries.CountryFile

/**
 * Owner File
 */
case class Owner(tag: CountryTag) extends Ordered[Owner] {

  /**
   * returns whether the owner country represented by this record is the same country as represented by
   * the 'other' country/owner
   */
  def isCountry(other: Any): Boolean = other match {
    case o: Owner => this.tag == o.tag
    case t: CountryTag => this.tag == t
    case c: CountryFile => this.tag == c.countryTag
    case _ => false
  }

  override def compare(o: Owner): Int = this.tag.compareTo(o.tag)
}