package hoi4utils.clausewitz_coding.code;

import java.util.Objects;

public class ClausewitzDate {
	public static final int DEFAULT_HOUR = 12;
	private static ClausewitzDate current = new ClausewitzDate();
	private final int year;
	private final int month;
	private final int day;
	private final int hour;

	public ClausewitzDate() {
		this("1936.1.1.12");
	}

	public ClausewitzDate(int year, int month, int day) {
		this(year, month, day, 12);
	}

	public ClausewitzDate(int year, int month, int day, int hour) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
	}

	public ClausewitzDate(String s) {
		s = s.trim();

		int pos1;
		int pos2;

		// ? todo rewrite this parse it could be better
		/* year */
		pos1 = 0;
		pos2 = s.indexOf('.');
		if (pos2 <= 0) {
			throw new IllegalArgumentException(s + " is not a valid clausewitz date");
		}
		year = Integer.parseInt(s.substring(pos1, pos2));

		/* month */
		pos1 = pos2 + 1;
		pos2 = s.indexOf('.', pos1);
		if (pos2 <= pos1) {
			throw new IllegalArgumentException(s + " is not a valid clausewitz date");
		}
		month = Integer.parseInt(s.substring(pos1, pos2));

		/* day */
		pos1 = pos2 + 1;
		pos2 = s.indexOf('.', pos1);
		if (pos2 <= pos1) {
			throw new IllegalArgumentException(s + " is not a valid clausewitz date");
		}
		day = Integer.parseInt(s.substring(pos1, pos2));

		/* hour */
		int temphour;
		pos1 = pos2 + 1;
		pos2 = s.indexOf('.', pos1);
		if (pos2 <= pos1) {
			temphour = DEFAULT_HOUR;			  // valid to not have hour (in case, default)
		} else {
			temphour = Integer.parseInt(s.substring(pos1, pos2));
			if (temphour < 0 || temphour > 24) {
				temphour = DEFAULT_HOUR;		  // just default it
			}
		}
		hour = temphour;				// hour is final
	}

	public static ClausewitzDate current() {
		return current;
	}

	public String toString() {
		return year + "." + month + "." + day + "." + hour;
	}

	public String date() {
		return year + "." + month + "." + day;
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof ClausewitzDate date)) {
			return false;
		}

		return this.date().equals(date.date());
	}

	@Override
	public int hashCode() {
		return Objects.hash(year, month, day);	    // objects with different hours will have the same hash code
													// as they are considered equal by equals().    // todo is this okay?
	}
}
