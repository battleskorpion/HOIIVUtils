package com.hoi4utils.clausewitz.data.units;

import com.hoi4utils.clausewitz.HOIIVUtilsInitializer;
import com.hoi4utils.clausewitz.data.country.Character;
import com.hoi4utils.clausewitz.data.country.CountryFlag;
import com.hoi4utils.clausewitz.data.idea.Idea;
import com.hoi4utils.clausewitz.data.technology.Technology;

import java.util.Set;

// todo convert to scala code
/**
 * Refer to <url> <a href="https://hoi4.paradoxwikis.com/Division_modding">HOI4 Division Modding Wiki</a> </url>
 * todo
 */
public class OrdersOfBattle {
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
    private final int convoys = 0;
    private Set<Idea> ideas;
    private Politics politics;
    private Popularities popularities;
    private Set<Character> recruitCharacters;
    private Character countryLeader; // todo ?
}
