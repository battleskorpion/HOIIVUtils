package com.hoi4utils.hoi4mod.localization

import com.hoi4utils.hoi4mod.localization.Property.DESCRIPTION

trait HasDesc {

}

// Only types that are BOTH Localizable and HasDesc get locDesc:
object LocalizableSyntax:
	extension (x: Localizable & HasDesc)
		def locDesc: Option[String] = x.localizationText(DESCRIPTION)

