package com.HOIIVUtils.hoi4utils.clausewitz_data.localization;

/**
 * This is the Localization record file.
 * // todo add explicit/default loc key
 */
public record Localization(String ID, String text, Status status) {

	public Localization(String id, String focusLoc) {
		this(id, focusLoc, Status.NEW);
	}

	public enum Status {
		DEFAULT,
		EXISTS, // localization already exists in a localization file (was only read into the
				// program)
		NEW, // localization is new.
		UPDATED, // localization already exists in a loc file, but this is an updated version.
	}

	public String toString() {
		return ID + ":0" + " " + "\"" + text + "\"";
	}

}
