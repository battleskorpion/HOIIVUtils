package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi4.localization.{Localizable, Property}
import com.hoi4utils.hoi42.common.country_tags.*
import com.hoi4utils.script2.datatype.*
import com.hoi4utils.hoi42.common.*
import com.hoi4utils.script2.*
import com.hoi4utils.script2.PDXPropertyValueExtensions.*


import java.io.File
import scala.reflect.ClassTag

class FocusTree(var treeRegistry: FocusTreeRegistry, var file: Option[File])(using Registry[SharedFocus])
  extends PDXEntity with FocusRegistry[Focus] with IDReferable[String] with RegistryMember[FocusTree](treeRegistry)
    with Localizable with PDXFile:

  given Registry[CountryTag] = new CountryTagRegistry()

  val id = pdx[String]("id") required true
  val country = pdx[FocusTreeCountry]("country")
  val focuses = pdxList[Focus]("focus") required true
  val default = pdx[Boolean]("default")
  val resetOnCivilWar = pdx[Boolean]("reset_on_civil_war")
  val continuousFocusPosition = pdx[PointPDX]("continuous_focus_position")
  //  val initialShowPosition = pdx[InitialShowPosition]("initial_show_position")
  val shortcut = pdx[Shortcut]("shortcut")
  //  /** special handling */
  val sharedFocuses = pdxList[Reference[SharedFocus]]("shared_focus")

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]

  // TODO fix code headOption is wrong but im lazy right now to do correct solution!!
  def countryTag: Option[CountryTag] = country
    .flatMap(_.modifier())
    .flatMap(_.headOption)
    .flatMap(_.tag.resolve)

  override def localizableProperties: Map[Property, String] =
    id.value match
      case Some(id) => Map(Property.NAME -> id)
      case None => Map() //properties.put(Property.NAME, null)

  /**
   * @inheritdoc
   *
   * The localizable group for a focus tree is the list of focuses.
   */
  override def getLocalizableGroup: Iterable[? <: Localizable] = focuses

object FocusTree { }

class FocusTreeRegistry extends Registry[FocusTree] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]
}

class FocusTreeCountry(using Registry[CountryTag]) extends PDXEntity:
  val base = pdx[Double]("base")
  val factor = pdx[Double]("factor")
  val add = pdx[Double]("add")
  val modifier = pdxList[TagModifier]("modifier")

//  override def getPDXTypeName: String = "AI Willingness"

class TagModifier(using Registry[CountryTag]) extends PDXEntity:
  val base = pdx[Double]("base")
  val factor = pdx[Double]("factor")
  val add = pdx[Double]("add")
  val tag = pdx[Reference[CountryTag]]("country")

//      override def getPDXTypeName: String = "Modifier"

/*class InitialShowPosition extends PDXEntity:

  */

class Shortcut(using Registry[CountryTag], Registry[Focus]) extends PDXEntity:
  val name = pdx[String]("name")
  val target = pdx[Reference[Focus]]("target")
  val scrollWheelFactor = pdx[Double]("scroll_wheel_factor") // TODO ExpectedRange.ofUnitInterval
//  val trigger = pdx[TriggerPDX]("trigger")  // TODO IMPL TRIGGER PDX



