package com.HOIIVUtils.hoi4utils.clausewitz_code;

import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.chrono.IsoChronology;
import java.time.temporal.*;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;

public class ClausewitzDate implements Temporal, TemporalAdjuster, Comparable<ClausewitzDate> {
	/**
	 * @see LocalDate#MIN
	 */
	public static final LocalDate MIN = LocalDate.MIN;
	/**
	 * @see LocalDate#MAX
	 */
	public static final LocalDate MAX = LocalDate.MAX;

	public static final int DEFAULT_HOUR = 12;
	private static final ClausewitzDate DEFAULT_DATE = ClausewitzDate.of();
	private final int year;
	private final int month;
	private final int day;
	private final int hour;

	private ClausewitzDate(int year, int month, int day, int hour) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
	}

	public static ClausewitzDate of() {
		return new ClausewitzDate(1936, 1, 1, DEFAULT_HOUR);
	}

	public static ClausewitzDate of(int year, int month, int day) {
		return new ClausewitzDate(year, month, day, 12);
	}

	public static ClausewitzDate of(int year, int month, int day, int hour) {
		YEAR.checkValidValue(year);
		MONTH_OF_YEAR.checkValidValue(month);
		DAY_OF_MONTH.checkValidValue(day);
		HOUR_OF_DAY.checkValidValue(hour);
		return create(year, month, day, hour);
	}

	private static ClausewitzDate create(int year, int month, int day, int hour) {
		if (day > 28) {
			int dom = switch (month) {
				case 2 -> (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
				case 4, 6, 9, 11 -> 30;
				default -> 31;
			};
			if (day > dom) {
				if (day == 29) {
					throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
				} else {
					throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + day + "'");
				}
			}
		}
		return new ClausewitzDate(year, month, day, hour);
	}

	public static ClausewitzDate of(String s) {
		s = s.trim();

		int pos1;
		int pos2;
		int year, month, day, hour;

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
			pos2 = s.length();
		}
		if (pos2 - pos1 == 1) {
			throw new IllegalArgumentException(s + " is not a valid clausewitz date");
		}
		day = Integer.parseInt(s.substring(pos1, pos2));

		/* hour */
		int temphour;
		pos1 = pos2 + 1;
		pos2 = s.length();
		if (pos2 <= (pos1 + 1)) {
			temphour = DEFAULT_HOUR;			  // valid to not have hour (in case, default)
		} else {
			temphour = Integer.parseInt(s.substring(pos1, pos2));
		}
		hour = temphour;				// hour is final

		return create(year, month, day, hour);
	}

	private static ClausewitzDate of(ClausewitzDate date, int hour) {
		return of(date.year, date.month, date.day, hour);
	}

	public ClausewitzDate atStartOfDay() {
		return ClausewitzDate.of(this, LocalTime.MIDNIGHT.getHour());
	}

	public static ClausewitzDate current() {
		return DEFAULT_DATE;
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

	/**
	 * A hash code for this date, based off of {@link LocalDate#hashCode()}, assuming
	 * {@link LocalDate#hashCode()} is more optimal than performing
	 * {@code Objects.hash(year, month, day)}.
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {
		int yearValue = year;
        return (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (month << 6) + (day));
	}

	@Override
	public int compareTo(@NotNull ClausewitzDate o) {
		return this.year > o.year ? 1 : this.year < o.year ? -1
				: this.month > o.month ? 1 : this.month < o.month ? -1
				: Integer.compare(this.day, o.day);
	}

	public int year() {
		return year;
	}

	public int month() {
		return month;
	}

	public int day() {
		return day;
	}

	/**
	 * Gets the hour of the day
	 *
	 * @return the hour, from 0 to 23
	 */
	public int hour() {
		return hour;
	}

	@Override
	public boolean isSupported(TemporalUnit unit) {
		switch (unit) {
            case DAYS:
			case MONTHS:
			case YEARS:
			case ChronoUnit.HOURS:
				return true;
            case null, default:
				return false;
		}
	}

	@Override
	public Temporal with(TemporalField field, long newValue) {
        return switch (field) {
            case YEAR -> new ClausewitzDate((int) newValue, month, day, hour);
            case MONTH_OF_YEAR -> new ClausewitzDate(year, (int) newValue, day, hour);
            case DAY_OF_MONTH -> new ClausewitzDate(year, month, (int) newValue, hour);
            case ChronoField.HOUR_OF_DAY -> new ClausewitzDate(year, month, day, (int) newValue);
            default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        };
	}

	@Override
	public Temporal plus(long amountToAdd, TemporalUnit unit) {
		return switch (unit) {
			case YEARS -> new ClausewitzDate(year + (int) amountToAdd, month, day, hour);
			case MONTHS -> new ClausewitzDate(year, month + (int) amountToAdd, day, hour);
			case DAYS -> new ClausewitzDate(year, month, day + (int) amountToAdd, hour);
			case HOURS -> new ClausewitzDate(year, month, day, hour + (int) amountToAdd);
			default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
		};
	}

	@Override
	public long until(Temporal endExclusive, TemporalUnit unit) {
		return switch (unit) {
			case DAYS, MONTHS, YEARS
					-> LocalDate.of(year, month, day).until(endExclusive, unit);
            case ChronoUnit.HOURS
		            -> LocalDateTime.of(year, month, day, hour, 0).until(endExclusive, unit);
			default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
		};
	}

	@Override
	public boolean isSupported(TemporalField field) {
		switch (field) {
			case YEAR:
			case MONTH_OF_YEAR:
			case DAY_OF_MONTH:
			case ChronoField.HOUR_OF_DAY:
				return true;
			case null, default:
				return false;
		}
	}

	@Override
	public long getLong(TemporalField field) {
		return switch (field) {
			case YEAR -> year;
			case MONTH_OF_YEAR -> month;
			case DAY_OF_MONTH -> day;
			case ChronoField.HOUR_OF_DAY -> hour;
			default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
		};
	}

	@Override
	public Temporal adjustInto(Temporal temporal) {
		return switch (temporal) {
			case LocalDate date -> date.withYear(year).withMonth(month).withDayOfMonth(day);
			case LocalDateTime dateTime -> dateTime.withYear(year).withMonth(month).withDayOfMonth(day).withHour(hour);
			default -> throw new UnsupportedTemporalTypeException("Unsupported temporal: " + temporal);
		};
	}

}
