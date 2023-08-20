package hoi4utils.clausewitz_coding.state;

import hoi4utils.clausewitz_coding.code.ClausewitzDate;
import hoi4utils.clausewitz_coding.country.CountryTag;
/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
