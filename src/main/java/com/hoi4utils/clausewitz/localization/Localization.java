package com.hoi4utils.clausewitz.localization;

import com.hoi4utils.clausewitz.exceptions.UnexpectedLocalizationStatusException;

import java.io.File;

/**
 * This is the Localization record file.
 * // todo add explicit/default loc key
 */
public record Localization(String ID, Integer version, String text, Status status) {

	public Localization(String id, String text, Status status) {
		this(id, null, text, status);
	}

	/**
	 * Creates a new localization with the given ID, and text. version optional
	 */
	public Localization(String key, String text) {
		this(key, null, text, Status.NEW);
	}

	public Localization(String key, String text, Integer version) {
		this(key, version, text, Status.NEW);
	}


	public enum Status {
//		DEFAULT,
		EXISTS, // localization already exists in a mod localization file (was only read into the
				// program)
		NEW,    // localization is new (not written to a loc file). If new localization is saved, it's ID should be unique.
		UPDATED, // localization key already exists in a loc file, but text is updated. This localization can overwrite the existing localization.
		VANILLA, // localization is from the base ('vanilla') game
		MISSING, // localization is completely missing (no localization key exists in the localization list)
	}

	public boolean isReplaceableBy(Localization other) throws UnexpectedLocalizationStatusException {
		switch (status) {
			case NEW:
				if (other.status != Status.NEW) throw new UnexpectedLocalizationStatusException(this, other);
				else return true;
			case EXISTS:
				if (other.status != Status.UPDATED) throw new UnexpectedLocalizationStatusException(this, other);
				else return true;
			case UPDATED:
				if (other.status != Status.UPDATED && other.status != Status.EXISTS) {
					throw new UnexpectedLocalizationStatusException(this, other);
				}
				else return true;
			case VANILLA:
				return false;
			default:
				return false;
		}
	}

	public boolean isReplaceable() {
		return switch (status) {
			case NEW -> true;
			case EXISTS -> true;
			case UPDATED -> true;
			case VANILLA -> false;
			default -> false;
		};
	}

	public boolean isNew() {
		return status == Status.NEW;
	}

	/**
	 * Replaces the text of this localization with the given text.
	 * @param text
	 * @return a new localization with the given text.
	 */
	public Localization replaceWith(String text) {
		return new Localization(ID, version, text, Status.UPDATED);
	}

	public Localization replaceWith(String text, Integer version, File file) {
		var status = switch (this.status) {
			case NEW -> Status.NEW;
			case EXISTS -> Status.UPDATED;
			case UPDATED -> Status.UPDATED;
			default -> throw new UnexpectedLocalizationStatusException(this);
		};
		return new Localization(ID, version, text, status);
	}

	public String toString() {
		return ID + ":" + (version == null? "" : version) + " " + "\"" + text + "\"";
	}

	public String baseKey() {
		if (ID.endsWith("_desc")) {
			return ID.substring(0, ID.length() - 5);
		} else return ID; 
	}

}
