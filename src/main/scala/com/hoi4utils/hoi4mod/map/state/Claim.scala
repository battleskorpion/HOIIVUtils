package com.hoi4utils.hoi4mod.map.state

import com.hoi4utils.hoi4mod.common.country_tags.CountryTag
import com.hoi4utils.parser.ClausewitzDate

/**
 * Claim File
 */
case class Claim(tag: CountryTag, state: State, date: ClausewitzDate, definedInHistory: Boolean)