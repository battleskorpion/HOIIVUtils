package com.hoi4utils.hoi42.common.units

import com.hoi4utils.hoi4.common.technologies.Technology
import com.hoi4utils.hoi4.history.countries.CountryFlag
import com.hoi4utils.script2.{IDReferable, NameReferable, PDXDecoder, PDXEntity, PDXProperty, Registry, RegistryMember}

import java.io.File
import java.util


// todo convert to scala code// todo convert to scala code

/**
 * Refer to <url> <a href="https://hoi4.paradoxwikis.com/Division_modding">HOI4 Division Modding Wiki</a> </url>
 * todo
 */
class OrdersOfBattle(var oobs: OrdersOfBattleRegistry, var file: Option[File]) extends PDXEntity with IDReferable[String] with RegistryMember[OrdersOfBattle](oobs){
  // private Set<DivisionTemplate> divisionTemplates
  // set of division templates defined in oob
  // Set<effect> instantEffects
  // effect/instant effect?  (when oob loaded do this, like add eq. prod.)
  /* mostly just load the oob and its date */
  val id = pdx[String]("id") required true //new StringPDX("id")
  val capital = 0
  val defaultResearchSlots = 0 // default research slots

  val countryFlags: util.Set[CountryFlag] = null
  val stability = .0 // stability percentage defined from 0.0-1.0

  val warSupport = .0 // war support percentage defined from 0.0-1.0

  val startingTech: util.Set[Technology] = null // starting technology defined in history/countries file

  final private val convoys = 0
  //  private val ideas: util.Set[Idea] = null
  //private val politics: Politics = null
  //private val popularities: Popularities = null
  private val recruitCharacters: util.Set[Character] = null
  private val countryLeader: Character = null // todo ?

  override def idProperty: PDXProperty[String] = id
}

class OrdersOfBattleRegistry extends Registry[OrdersOfBattle] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}
