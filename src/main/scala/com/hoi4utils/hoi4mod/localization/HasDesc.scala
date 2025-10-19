package com.hoi4utils.hoi4mod.localization

import com.hoi4utils.hoi4mod.localization.Property.DESCRIPTION

trait HasDesc {

}

// Only types that are BOTH Localizable and HasDesc get locDesc:
object LocalizableSyntax:
	extension (localized: Localizable & HasDesc)
		def locDesc: Option[String] = localized.localizationText(DESCRIPTION)

		/**
		 * Sets the name localization to the new value.
		 *
		 * @param text the new localization text.
		 */
		def replaceDesc(text: String): Unit = localized.replaceLocalization(DESCRIPTION, text)

