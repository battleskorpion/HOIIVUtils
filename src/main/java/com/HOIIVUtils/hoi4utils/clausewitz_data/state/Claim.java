package com.HOIIVUtils.hoi4utils.clausewitz_data.state;

import com.HOIIVUtils.hoi4utils.clausewitz_code.ClausewitzDate;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag;
/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
