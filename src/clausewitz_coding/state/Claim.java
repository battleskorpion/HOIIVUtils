package clausewitz_coding.state;

import clausewitz_coding.code.ClausewitzDate;
import clausewitz_coding.country.CountryTag;
/*
 * Claim File
 */
public record Claim (CountryTag tag, State state, ClausewitzDate date, boolean definedInHistory) {

}
