package com.HOIIVUtils.clauzewitz.map.state;

import com.HOIIVUtils.clauzewitz.code.ClausewitzDate;
import com.HOIIVUtils.clauzewitz.data.country.CountryTag;

/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
