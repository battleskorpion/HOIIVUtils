package com.HOIIVUtils.clauzewitz.data.units;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.data.country.CountryFlag;
import com.HOIIVUtils.clauzewitz.data.idea.Idea;
import com.HOIIVUtils.clauzewitz.data.technology.Technology;
import com.HOIIVUtils.clauzewitz.data.country.Character;

import java.util.Set;

/**
 * Refer to <url> <a href="https://hoi4.paradoxwikis.com/Division_modding">HOI4 Division Modding Wiki</a> </url>
 * todo
 */
public class OrdersOfBattle extends HOIIVFile {
	// private Set<DivisionTemplate> divisionTemplates
	// set of division templates defined in oob
	// Set<effect> instantEffects
	// effect/instant effect?  (when oob loaded do this, like add eq. prod.)

    /* mostly just load the oob and its date */

    String oob_id;
    private int capital;
    private int defaultResearchSlots;               // default research slots
    private Set<CountryFlag> countryFlags;
    private double stability;                       // stability percentage defined from 0.0-1.0
    private double warSupport;                      // war support percentage defined from 0.0-1.0
    private Set<Technology> startingTech;           // starting technology defined in history/countries file
    private int convoys = 0;
    private Set<Idea> ideas;
    private Politics politics;
    private Popularities popularities;
    private Set<Character> recruitCharacters;
    private Character countryLeader; // todo ?
}
