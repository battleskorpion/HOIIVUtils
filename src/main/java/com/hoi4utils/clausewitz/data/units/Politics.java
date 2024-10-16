package com.hoi4utils.clausewitz.data.units;

import com.hoi4utils.clausewitz.code.ClausewitzDate;

/**
 * ex:
 * 	ruling_party = neutrality
 * 	last_election = "1936.1.1"
 * 	election_frequency = 48
 * 	elections_allowed = yes
 *
 * @param party
 * @param lastElection
 * @param electionFrequency
 * @param elections_allowed
 */
public record Politics(PoliticalParty party, ClausewitzDate lastElection, int electionFrequency, boolean elections_allowed) {

}
