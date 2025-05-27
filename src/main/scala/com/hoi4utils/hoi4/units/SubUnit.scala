package com.hoi4utils.hoi4.units

import com.hoi4utils.BoolType
import com.hoi4utils.parser.{Node, Parser, ParserException}

import java.io.File
import javax.swing.JOptionPane
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*

/**
 * A single sub‑unit definition in HOI4 (e.g. combat battalion or support company).
 *
 * @param identifier        The script ID of the sub‑unit
 * @param abbreviation      Abbreviation string (if present)
 * @param sprite            Path to sprite (if present)
 * @param mapIconCategory   Map icon category (if present)
 * @param priority          Spawn priority (if present)
 * @param aiPriority        AI priority (if present)
 * @param active            Whether this sub‑unit is active (if present)
 * @param group             Group identifier (if present)
 * @param combatWidth       Combat width (if present)
 * @param manpower          Manpower cost (if present)
 * @param maxOrganization   Maximum organization (if present)
 * @param defaultMorale     Default morale (if present)
 * @param maxStrength       Maximum strength (if present)
 * @param trainingTime      Training time in days (if present)
 * @param weight            Unit weight (if present)
 * @param supplyConsumption Supply consumption (if present)
 */
case class SubUnit(
                    identifier: String,
                    abbreviation: Option[String],
                    sprite: Option[String],
                    mapIconCategory: Option[String],
                    priority: Option[Int],
                    aiPriority: Option[Int],
                    active: Option[Boolean],
                    group: Option[String],
                    combatWidth: Option[Int],
                    manpower: Option[Int],
                    maxOrganization: Option[Int],
                    defaultMorale: Option[Int],
                    maxStrength: Option[Int],
                    trainingTime: Option[Int],
                    weight: Option[Double],
                    supplyConsumption: Option[Double]
                  )

object SubUnit {
  /** Read all sub‑unit definitions from files in the given directory. */
  def read(dir: File): List[SubUnit] = {
    if (!dir.isDirectory) throw new IllegalArgumentException(s"$dir is not a directory")

    val files: List[File] = Option(dir.listFiles())
      .map(_.toList.filter(_.isFile))
      .getOrElse(Nil)
    if (files.isEmpty) throw new IllegalArgumentException(s"$dir is empty or has no files")

    val buf = ListBuffer.empty[SubUnit]

    files.foreach { file =>
      val root = try new Parser(file).parse
      catch {
        case e: ParserException =>
          JOptionPane.showMessageDialog(null,
            s"[SubUnit] Error parsing ${file.getName}: ${e.getMessage}")
          return buf.toList   // or simply skip this file
      }
      
      val nodes: Seq[Node] = root.find("sub_units").toSeq
      if (nodes.isEmpty) {
        println(s"No sub_units found in ${file.getName}")
      } else {
        println(s"File: ${file.getName}, subunits: ${nodes.size}")
        buf ++= createSubUnits(nodes)
      }
    }
    buf.toList
  }

  /** Convert parsed nodes into SubUnit instances. */
  private def createSubUnits(nodes: Seq[Node]): Seq[SubUnit] = {
    nodes.map { node =>
      SubUnit(
        node.name,
        if (node.contains("abbreviation")) Some(node.getValue("abbreviation").asString) else None,
        if (node.contains("sprite")) Some(node.getValue("sprite").asString) else None,
        if (node.contains("map_icon_category")) Some(node.getValue("map_icon_category").asString) else None,
        if (node.contains("priority")) Some(node.getValue("priority").$intOrElse(0)) else None,
        if (node.contains("ai_priority")) Some(node.getValue("ai_priority").$intOrElse(0)) else None,
        if (node.contains("active")) Some(node.getValue("active").asBool(BoolType.YES_NO)) else None,
        //subUnit.type//subUnit.type
        if (node.contains("group")) Some(node.getValue("group").asString) else None,
        //subUnit.categories//subUnit.categories
        if (node.contains("combat_width")) Some(node.getValue("combat_width").$intOrElse(0)) else None,
        //subUnit.need//subUnit.need
        if (node.contains("manpower")) Some(node.getValue("manpower").$intOrElse(0)) else None,
        if (node.contains("max_organization")) Some(node.getValue("max_organization").$intOrElse(0)) else None,
        if (node.contains("default_morale")) Some(node.getValue("default_morale").$intOrElse(0)) else None,
        if (node.contains("max_strength")) Some(node.getValue("max_strength").$intOrElse(0)) else None,
        if (node.contains("training_time")) Some(node.getValue("training_time").$intOrElse(0)) else None,
        if (node.contains("weight")) Some(node.getValue("weight").$doubleOrElse(0.0)) else None,
        if (node.contains("supply_consumption")) Some(node.getValue("supply_consumption").$doubleOrElse(0.0)) else None
      )
    }
  }

  /** Data accessor functions, for table display: each SubUnit → a field value. */
  def dataFunctions(): Seq[SubUnit => Any] = Seq(
    _.identifier,
    _.abbreviation,
    _.sprite,
    _.mapIconCategory,
    _.priority,
    _.aiPriority,
    _.active,
    _.group,
    _.combatWidth,
    _.manpower,
    _.maxOrganization,
    _.defaultMorale,
    _.maxStrength,
    _.trainingTime,
    _.weight,
    _.supplyConsumption
  )

  /** Labels corresponding to the dataFunctions order. */
  def dataLabels(): Seq[String] = Seq(
    "Subunit",
    "Abbreviation",
    "Sprite",
    "Map Icon Category",
    "Priority",
    "AI Priority",
    "Active",
    "Group",
    "Combat Width",
    "Manpower",
    "Max Organization",
    "Default Morale",
    "Max Strength",
    "Training Time",
    "Weight",
    "Supply Consumption"
  )
}
