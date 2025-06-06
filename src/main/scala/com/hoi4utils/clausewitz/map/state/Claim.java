package com.hoi4utils.clausewitz.map.state;

import com.hoi4utils.ClausewitzDate;
import com.hoi4utils.hoi4.country.CountryTag;
import com.map.State;

/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
