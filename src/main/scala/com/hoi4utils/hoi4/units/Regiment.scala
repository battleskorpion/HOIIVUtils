package com.hoi4utils.hoi4.units

/** The kind of subunit in a regiment. */
enum SubunitType:
  case CombatBattalion, SupportCompany

/**
 * A single regiment in a division template, positioned at (x,y). 
 * @param x  horizontal slot in the template
 * @param y  vertical slot in the template
 */
case class Regiment(x: Int, y: Int)