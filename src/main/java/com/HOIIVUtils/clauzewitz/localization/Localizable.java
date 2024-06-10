package com.HOIIVUtils.clauzewitz.localization;

// todo accept Localization class as element <> ?
public interface Localizable {
	/**
	 * ex: HOI4 focuses have two localizable properties: name, description.
	 * @return
	 */
	default int numLocalizableProperties() {
		return 1;
	}


}
