package com.HOIIVUtils.hoi4utils.clausewitz_code;

import java.time.DateTimeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClausewitzDateTest {

    @org.junit.jupiter.api.Test
    void current () {
        ClausewitzDate current = ClausewitzDate.of();
        assertEquals(1936, current.year());
        assertEquals(1, current.month());
        assertEquals(1, current.day());
        assertEquals(12, current.hour());
    }

    @org.junit.jupiter.api.Test
    void date1() {
        ClausewitzDate date = ClausewitzDate.of(1936, 1, 1);
        assertEquals(1936, date.year());
        assertEquals(1, date.month());
        assertEquals(1, date.day());
        assertEquals(12, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date2() {
        ClausewitzDate date = ClausewitzDate.of("1936.1.1.12");
        assertEquals(1936, date.year());
        assertEquals(1, date.month());
        assertEquals(1, date.day());
        assertEquals(12, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date3() {
        ClausewitzDate date = ClausewitzDate.of("1940.1.1.12");
        assertEquals(1940, date.year());
        assertEquals(1, date.month());
        assertEquals(1, date.day());
        assertEquals(12, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date4() {
        ClausewitzDate date = ClausewitzDate.of("1776.1.1.19");
        assertEquals(1776, date.year());
        assertEquals(1, date.month());
        assertEquals(1, date.day());
        assertEquals(19, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date5() {
        ClausewitzDate date = ClausewitzDate.of("1941.12.31.23");
        assertEquals(1941, date.year());
        assertEquals(12, date.month());
        assertEquals(31, date.day());
        assertEquals(23, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date6() {
        ClausewitzDate date = ClausewitzDate.of("1941.12.31.0");
        assertEquals(1941, date.year());
        assertEquals(12, date.month());
        assertEquals(31, date.day());
        assertEquals(0, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date7() {
        ClausewitzDate date = ClausewitzDate.of("1941.12.31.1");
        assertEquals(1941, date.year());
        assertEquals(12, date.month());
        assertEquals(31, date.day());
        assertEquals(1, date.hour());
    }

    @org.junit.jupiter.api.Test
    void date_exc1() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.2.30.1"));
    }

    @org.junit.jupiter.api.Test
    void date_exc2() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.2.31.1"));
    }

    @org.junit.jupiter.api.Test
    void date_exc3() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.13.31.1"));
    }

    @org.junit.jupiter.api.Test
    void date_exc4() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.12.32.1"));
    }

    @org.junit.jupiter.api.Test
    void date_exc5() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.12.31.25"));
    }

    @org.junit.jupiter.api.Test
    void date_exc6() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.12.31.-1"));
    }

    @org.junit.jupiter.api.Test
    void date_exc7() {
        assertThrows(IllegalArgumentException.class, () -> ClausewitzDate.of("1941."));
    }

    @org.junit.jupiter.api.Test
    void date_exc8() {
        assertThrows(IllegalArgumentException.class, () -> ClausewitzDate.of("1941.12"));
    }

    @org.junit.jupiter.api.Test
    void date_exc9() {
        assertThrows(DateTimeException.class, () -> ClausewitzDate.of("1941.13.32.25"));
    }

    @org.junit.jupiter.api.Test
    void testEquals() {
        ClausewitzDate date1 = ClausewitzDate.of(1936, 1, 1);
        ClausewitzDate date2 = ClausewitzDate.of(1936, 1, 1);
        assertEquals(date1, date2);
    }

    @org.junit.jupiter.api.Test
    void testHashCode() {
        ClausewitzDate date1 = ClausewitzDate.of(1936, 1, 1);
        ClausewitzDate date2 = ClausewitzDate.of(1936, 1, 1);
        assertEquals(date1.hashCode(), date2.hashCode());
    }

    @org.junit.jupiter.api.Test
    void compareTo() {
        ClausewitzDate date1 = ClausewitzDate.of(1936, 1, 1);
        ClausewitzDate date2 = ClausewitzDate.of(1936, 1, 1);
        assertEquals(0, date1.compareTo(date2));
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        ClausewitzDate date = ClausewitzDate.of(1936, 1, 1);
        assertEquals("1936.1.1.12", date.toString());
    }

    @org.junit.jupiter.api.Test
    void testToString2() {
        ClausewitzDate date = ClausewitzDate.of(1936, 1, 1, 1);
        assertEquals("1936.1.1.1", date.toString());
    }

}