package com.hoi4utils.hoi4mod.common.units

/**
 * Refer to HOI4 Division Modding Wiki:
 * https://hoi4.paradoxwikis.com/Division_modding
 */
case class DivisionTemplate(
                             /** Unique identifier for this division template */
                             name: String,

                             /** Optional grouping key for division names */
                             divisionNamesGroup: Option[String],

                             /** Combat battalions in this division */
                             regiments: Set[Regiment],

                             /** Support companies; may be empty */
                             support: Set[Regiment] = Set.empty,

                             /** Template priority (0–2, default 1) */
                             priority: Int = 1,

                             /** If true, division is locked (refer to wiki) */
                             isLocked: Boolean = false,

                             /** If true, force allow recruiting (refer to wiki) */
                             forceAllowRecruiting: Boolean = false,

                             /** Maximum number of divisions of this type; -1 for no cap */
                             divisionCap: Int = -1
                           )
