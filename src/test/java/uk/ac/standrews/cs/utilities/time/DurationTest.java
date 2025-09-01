/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.time;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class DurationTest {

    @Test
    public void durationOrdering() {

        final Duration d1 = new Duration(1, TimeUnit.MILLISECONDS);
        final Duration d2 = new Duration(10, TimeUnit.MILLISECONDS);
        final Duration d3 = new Duration(1, TimeUnit.SECONDS);

        assertEquals(0, d1.compareTo(d1));
        assertEquals(-1, d1.compareTo(d2));
        assertEquals(-1, d1.compareTo(d3));

        assertEquals(1, d2.compareTo(d1));
        assertEquals(0, d2.compareTo(d2));
        assertEquals(-1, d2.compareTo(d3));

        assertEquals(1, d3.compareTo(d1));
        assertEquals(1, d3.compareTo(d2));
        assertEquals(0, d3.compareTo(d3));
    }

    @Test
    public void conversion() {

        final Duration d = new Duration(1, TimeUnit.HOURS);

        assertEquals(3600L, d.convertTo(TimeUnit.SECONDS).getLength());
    }

    @Test
    public void toStringAsLargestTimeUnit() {

        final Duration one_ns = new Duration(1, TimeUnit.NANOSECONDS);
        final Duration one_ms_in_ns = new Duration(1000000, TimeUnit.NANOSECONDS);
        final Duration one_s_in_micros = new Duration(1000000, TimeUnit.MICROSECONDS);
        final Duration sixteen_mins_ish_in_millis = new Duration(1000000, TimeUnit.MILLISECONDS);
        final Duration eleven_days_ish_in_seconds = new Duration(1000000, TimeUnit.SECONDS);
        final Duration one_day = new Duration(1, TimeUnit.DAYS);

        assertEquals("1 ns", one_ns.toStringAsLargestTimeUnit());
        assertEquals("1 ms", one_ms_in_ns.toStringAsLargestTimeUnit());
        assertEquals("1 s", one_s_in_micros.toStringAsLargestTimeUnit());
        assertEquals("16 min", sixteen_mins_ish_in_millis.toStringAsLargestTimeUnit());
        assertEquals("11 days", eleven_days_ish_in_seconds.toStringAsLargestTimeUnit());
        assertEquals("1 days", one_day.toStringAsLargestTimeUnit());
    }
}
