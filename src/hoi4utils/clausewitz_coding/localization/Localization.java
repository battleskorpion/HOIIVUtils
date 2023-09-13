package hoi4utils.clausewitz_coding.localization;

/**
 * This is the Localization record file.
 */
public record Localization(String ID, String text, Status status) {

	public Localization(String id, String focusLoc) {
		this(id, focusLoc, Status.NEW);
	}

	public enum Status {
		EXISTS,		    // localization already exists in a localization file (was only read into the program
		NEW,			// localization is new.
		UPDATED,        // localization already exists in a loc file, but this is an updated version.
		DEFAULT,
	};

	public String toString() {
		return ID + ":0" + " " + "\"" + text + "\"";
	}

}
