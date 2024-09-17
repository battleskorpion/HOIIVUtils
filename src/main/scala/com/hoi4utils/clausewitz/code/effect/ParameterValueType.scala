package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.data.technology.TechCategory
import com.hoi4utils.clausewitz.map.state.State
import com.hoi4utils.clausewitz.script._
import java.util

enum ParameterValueType(val identifiers: List[String]) {
  case ace_type           extends ParameterValueType(List())
  case ai_strategy        extends ParameterValueType(List())
  case character          extends ParameterValueType(List("character"))
  case country            extends ParameterValueType(List("country", "tag"))
  case cw_bool            extends ParameterValueType(List("bool", "boolean"))
  case cw_float           extends ParameterValueType(List("float", "fraction"))
  case cw_int             extends ParameterValueType(List("int", "integer"))
  case cw_list            extends ParameterValueType(List("list"))
  case cw_string          extends ParameterValueType(List("string"))
  case cw_trait           extends ParameterValueType(List())
//  case cw_variable      extends ParameterValueType(List())
  case decision           extends ParameterValueType(List("decision"))
  case doctrine_category  extends ParameterValueType(List())
  case flag               extends ParameterValueType(List())
  case idea               extends ParameterValueType(List())
  case mission            extends ParameterValueType(List("mission"))
  case modifier           extends ParameterValueType(List())
  case scope              extends ParameterValueType(List())
  case state              extends ParameterValueType(List("state", "state_id"))
  case equipment          extends ParameterValueType(List())
  case strategic_region   extends ParameterValueType(List("strategic_region", "strat_region"))
  case building           extends ParameterValueType(List())
  case operation_token    extends ParameterValueType(List())
  case ideology           extends ParameterValueType(List())
  case sub_ideology       extends ParameterValueType(List())
  case province           extends ParameterValueType(List("province", "province_id"))
  case resource           extends ParameterValueType(List("resource"))
  case tech_category      extends ParameterValueType(List("tech_category", "technology_category"))
  case advisor_slot       extends ParameterValueType(List())
  case event              extends ParameterValueType(List())
  case wargoal            extends ParameterValueType(List("wargoal", "war_goal"))

}