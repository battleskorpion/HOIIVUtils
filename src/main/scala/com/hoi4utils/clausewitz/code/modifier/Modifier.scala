package com.hoi4utils.clausewitz.code.modifier

//import java.sql.Array;//import java.sql.Array;

//import java.util.ArrayList;//import java.util.ArrayList;

import com.hoi4utils.clausewitz.script.PDXScript

import java.util
import java.util.{EnumSet, SortedMap, TreeMap}


/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Modifiers">Modifiers
 * Wiki</a>
 * Notes:
 * - A modifier with the value of 0 "will always do nothing."
 * - Negative modifiers will always work and have the opposite effect
 * - Opinion modifiers are not regular modifiers, and should therefore be
 * implemented separately.
 * - modifiers do not support if statements
 */
object Modifier {
  /**
   * decides the colour of the modifier's value itself. There are three values,
   * good, bad, and neutral. neutral is permamently yellow, while good turns the
   * positive values green and negative values red. bad is the reversal of good.
   */
  enum ColorType {
    case good, bad, neutral
  }

  enum ValueType {
    case number, percentage, percentage_in_hundred, yes_no
  }

  enum ValuePostfix {
    case days, hours, daily
  }

}

/**
 *
 * @param _colorType ex: ColorType.good;
 * @param _valueType ex: ValueType.percentage;
 * @param _precision HOI4 inherently does not support precision > 3.
 * @param _postfix default: ValuePostfix.none;
 * @param _category
 */
trait Modifier protected[modifier](private val _colorType: Modifier.ColorType, private val _valueType: Modifier.ValueType,
                         private[modifier] var _precision: Int, private val _postfix: Modifier.ValuePostfix,
                         private val _category: Set[ModifierCategory])
  extends PDXScript[?] with Cloneable {

  /**
   * Returns the category of the modifier
   *
   * @return modifier's category
   */
  def category = _category

  def identifier: String = identifier

  def colorType: Modifier.ColorType = _colorType

  def valueType: Modifier.ValueType = _valueType

  def postfix: Modifier.ValuePostfix = _postfix

  private[modifier] def isInCategory(checkedCategory: ModifierCategory) = _category.contains(checkedCategory)

  @throws[CloneNotSupportedException]
  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[Modifier]  // clone the object
    clone
  }
}
