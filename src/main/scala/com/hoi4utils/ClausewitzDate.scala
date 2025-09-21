package com.hoi4utils

import java.time.*
import java.time.chrono.IsoChronology
import java.time.temporal.*
import scala.util.Try

/**
 * A date/time type matching Paradox Clausewitz engine conventions: YYYY.MM.DD.HH
 * Implements Java Time Temporal & TemporalAdjuster interfaces for interop.
 */
case class ClausewitzDate private (
                                    year: Int,
                                    month: Int,
                                    day: Int,
                                    hour: Int
                                  ) extends Temporal with TemporalAdjuster with Ordered[ClausewitzDate]:
  import ChronoField.*
  import ChronoUnit.*

  override def isSupported(unit: TemporalUnit): Boolean = unit match
    case YEARS | MONTHS | DAYS | HOURS => true
    case _                              => false

  override def `with`(field: TemporalField, newValue: Long): Temporal = field match
    case YEAR             => copy(year = newValue.toInt)
    case MONTH_OF_YEAR    => copy(month = newValue.toInt)
    case DAY_OF_MONTH     => copy(day = newValue.toInt)
    case HOUR_OF_DAY      => copy(hour = newValue.toInt)
    case _                => throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")

  override def plus(amountToAdd: Long, unit: TemporalUnit): Temporal = unit match
    case YEARS  => copy(year = year + amountToAdd.toInt)
    case MONTHS => copy(month = month + amountToAdd.toInt)
    case DAYS   => copy(day = day + amountToAdd.toInt)
    case HOURS  => copy(hour = hour + amountToAdd.toInt)
    case _      => throw new UnsupportedTemporalTypeException(s"Unsupported unit: $unit")

  override def until(endExclusive: Temporal, unit: TemporalUnit): Long = unit match
    case YEARS | MONTHS | DAYS => LocalDate.of(year, month, day).until(endExclusive, unit)
    case HOURS                 => LocalDateTime.of(year, month, day, hour, 0).until(endExclusive, unit)
    case _                     => throw new UnsupportedTemporalTypeException(s"Unsupported unit: $unit")

  override def isSupported(field: TemporalField): Boolean = field match
    case YEAR | MONTH_OF_YEAR | DAY_OF_MONTH | HOUR_OF_DAY => true
    case _                                                 => false

  override def getLong(field: TemporalField): Long = field match
    case YEAR           => year
    case MONTH_OF_YEAR  => month
    case DAY_OF_MONTH   => day
    case HOUR_OF_DAY    => hour
    case _              => throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")

  override def adjustInto(temporal: Temporal): Temporal = temporal match
    case dt: LocalDate     => dt.withYear(year).withMonth(month).withDayOfMonth(day)
    case dt: LocalDateTime => dt.withYear(year).withMonth(month).withDayOfMonth(day).withHour(hour)
    case _                  => throw new UnsupportedTemporalTypeException(s"Unsupported temporal: $temporal")

  override def compare(that: ClausewitzDate): Int =
    java.util.Comparator
      .comparingInt[ClausewitzDate](_.year)
      .thenComparingInt(_.month)
      .thenComparingInt(_.day)
      .compare(this, that)

  override def toString: String = s"$year.$month.$day.$hour"
  def YMDString: String          = s"$year.$month.$day"

  override def equals(obj: Any): Boolean = obj match
    case that: ClausewitzDate => this.YMDString == that.YMDString
    case _                    => false

//  override def hashCode(): Int = {
//    val y = year
//    (y & 0xFFFFF800) ^ ((y << 11) + (month << 6) + day)
//  }

object ClausewitzDate:
  val MIN: LocalDate  = LocalDate.MIN
  val MAX: LocalDate  = LocalDate.MAX

  private val DEFAULT_HOUR = 12
  private val MIN_LEGAL_HOUR = 0
  private val MAX_LEGAL_HOUR = 23

  private def create(year: Int, month: Int, day: Int, hour: Int): ClausewitzDate =
    if hour < 0 || hour > 24 then throw DateTimeException(s"Invalid hour: $hour")
    if month < 1 || month > 12 then throw DateTimeException(s"Invalid month: $month")

    // Additional day-of-month check
    if day > 28 then
      val dom = month match
        case 2 => if IsoChronology.INSTANCE.isLeapYear(year) then 29 else 28
        case m if Seq(4, 6, 9, 11).contains(m) => 30
        case _                                 => 31
      if day > dom then
        if dom == 29 then throw DateTimeException(s"Invalid date February 29 in $year")
        else throw DateTimeException(s"Invalid date ${Month.of(month).name} $day")

    ClausewitzDate(year, month, day, hour)

  /** Default start date: 1936.1.1 at noon */
  def of(): ClausewitzDate = ClausewitzDate(1936, 1, 1, DEFAULT_HOUR)

  def of(year: Int, month: Int, day: Int): ClausewitzDate = of(year, month, day, DEFAULT_HOUR)

  def of(year: Int, month: Int, day: Int, hour: Int): ClausewitzDate =
    create(year, month, day, hour)

  /** Parse strings like "YYYY.MM.DD" or "YYYY.MM.DD.HH" */
  def of(s: String): ClausewitzDate =
    val parts = s.trim.split("\\.")
    if parts.length < 3 || parts.length > 4 then
      throw IllegalArgumentException(s"'$s' is not a valid Clausewitz date")

    val Array(y, mo, d, h) = parts.padTo(4, "").map:
      case str if str.nonEmpty => str.toIntOption.getOrElse(
        throw IllegalArgumentException(s"'$s' is not a valid Clausewitz date")
      )
      case _ => DEFAULT_HOUR
    create(y, mo, d, h)

  /** Hours only adjuster */
  def of(date: ClausewitzDate, hour: Int): ClausewitzDate =
    create(date.year, date.month, date.day, hour)

  def defaulty: ClausewitzDate = of()

  def validDate(s: String): Boolean = Try(of(s)).isSuccess