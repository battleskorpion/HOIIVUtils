package com.hoi4utils.hoi4.scope

/**
 * Scopes can be divided into types by the targets of the scope for which effects are executed
 * or triggers are checked:
 * <p>
 * Country scopes - Executed for countries.<p>
 * State scopes - Executed for states.<p>
 * Character scopes - Executed for characters. Some subsets exist, such as unit leaders and country leaders.<p>
 * Division scopes - Executed for divisions.<p>
 * <a href="https://hoi4.paradoxwikis.com/Scopes">Hoi4 Wiki</a>
 */
enum ScopeType {
  case
  any,
  country,
  combatant, // todo is this a more specific country subset?
  state,
  character, // todo subsets inc. LEADER, OPERATIVE,
  leader,
  operative,
  operation,
  division,
  mio,
  contract,
  UNKNOWN, /* special */
  self, // = "this".... ah java .... ah JVM
  root,
  from,
  prev,
  owner,
  controller,
  occupied,
  capital,
  strategic_region

  def valueFromString(str: String): ScopeType = {
    str match {
      case "this" => self
      case _ => try {
        ScopeType.valueOf(str)
      } catch {
        case _: IllegalArgumentException => UNKNOWN
      }
    }
  }
}
