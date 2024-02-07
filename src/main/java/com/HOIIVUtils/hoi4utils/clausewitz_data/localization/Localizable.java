package com.HOIIVUtils.hoi4utils.clausewitz_data.localization;

import java.lang.invoke.VarHandle;

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
