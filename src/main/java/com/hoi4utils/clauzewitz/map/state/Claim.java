package main.java.com.hoi4utils.clauzewitz.map.state;

import main.java.com.hoi4utils.clauzewitz.code.ClausewitzDate;
import main.java.com.hoi4utils.clauzewitz.data.country.CountryTag;

/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
