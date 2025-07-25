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
package uk.ac.standrews.cs.utilities;

import static java.util.concurrent.TimeUnit.*;

// TODO - move in time package
public class TimeManipulation {

    public static void reportElapsedTime(final long start_time) {

        System.out.println("Elapsed time: " + formatMillis(System.currentTimeMillis() - start_time));
    }

    @SuppressWarnings("WeakerAccess")
    public static String formatMillis(final long millis) {

        final long hours = MILLISECONDS.toHours(millis);
        long millis_remaining = millis - HOURS.toMillis(hours);
        final long minutes = MILLISECONDS.toMinutes(millis_remaining);
        millis_remaining = millis_remaining - MINUTES.toMillis(minutes);
        final long seconds = MILLISECONDS.toSeconds(millis_remaining);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
