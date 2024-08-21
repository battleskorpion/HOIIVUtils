package com.hoi4utils.clausewitz.map.state;

import com.hoi4utils.clausewitz.code.ClausewitzDate;
import com.hoi4utils.clausewitz.data.country.CountryTag;

/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
