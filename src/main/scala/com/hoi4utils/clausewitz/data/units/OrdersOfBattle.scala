package com.hoi4utils.clausewitz.data.units

import com.hoi4utils.clausewitz.HOIIVUtilsInitializer
import com.hoi4utils.clausewitz.data.country.Character
import com.hoi4utils.clausewitz.data.country.CountryFlag
import com.hoi4utils.clausewitz.data.idea.Idea
import com.hoi4utils.clausewitz.data.technology.Technology
import com.hoi4utils.clausewitz.script.StringPDX

import java.util
import java.util.Set


// todo convert to scala code// todo convert to scala code

/**
 * Refer to <url> <a href="https://hoi4.paradoxwikis.com/Division_modding">HOI4 Division Modding Wiki</a> </url>
 * todo
 */
class OrdersOfBattle {
  // private Set<DivisionTemplate> divisionTemplates
  // set of division templates defined in oob
  // Set<effect> instantEffects
  // effect/instant effect?  (when oob loaded do this, like add eq. prod.)
  /* mostly just load the oob and its date */
  final val id = new StringPDX("id")
  private val capital = 0
  private val defaultResearchSlots = 0 // default research slots

  private val countryFlags: util.Set[CountryFlag] = null
  private val stability = .0 // stability percentage defined from 0.0-1.0

  private val warSupport = .0 // war support percentage defined from 0.0-1.0

  private val startingTech: util.Set[Technology] = null // starting technology defined in history/countries file

  final private val convoys = 0
//  private val ideas: util.Set[Idea] = null
  private val politics: Politics = null
  private val popularities: Popularities = null
  private val recruitCharacters: util.Set[Character] = null
  private val countryLeader: Character = null // todo ?

}

object OrdersOfBattle {
  def list: List[OrdersOfBattle] = {
    List[OrdersOfBattle]()
  }
}
