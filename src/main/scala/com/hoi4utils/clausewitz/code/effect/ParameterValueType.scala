package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.data.technology.TechCategory
import com.hoi4utils.clausewitz.map.state.State
import com.hoi4utils.clausewitz.script.*
import org.sqlite.SQLiteConfig.Pragma

import java.util

object ParameterValueType {
  def of(s: String): Option[ParameterValueType] = {
    var str = if (s.startsWith("<") && s.endsWith(">")) {
      s.substring(1, s.length - 1)
    } else {
      s
    }
    str = str.replaceAll("([^\\s])(<)", "$1 $2")
    val args = str.split(" ")

    // 1st arg should be identifier of parameter value type
    // other args should be @<modifier name> .... <optional modifier arguments>
    ParameterValueType.values.find(_.identifiers.contains(args(0))) match {
      case Some(p) =>
        p.setIdentifier(args(0))
        Some(p)
      case None => None
    }
  }
}

enum ParameterValueType(val identifiers: List[String]) extends Enum[ParameterValueType] {
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

  private var _activeIdentifier: String = ""

  private def setIdentifier(identifier: String): Unit = {
    _activeIdentifier = identifier
  }

  def activeIdentifier: String = _activeIdentifier

}