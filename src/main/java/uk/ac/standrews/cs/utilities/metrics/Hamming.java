/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.BitSet;

public class Hamming extends Metric<BitSet> {

    @Override
    public String getMetricName() {
        return "Hamming";
    }

    /**
     * @param x - a bitset over which to calculate Hamming distance
     * @param y - a bitset over which to calculate Hamming distance
     * @return - return the normalised Hamming distance - if all bits are different return 1, if all the same return 0
     */
    public double calculateDistance(BitSet x, BitSet y) {

        long[] x_as_longs = x.toLongArray();
        long[] y_as_longs = y.toLongArray();

        if (x_as_longs.length != y_as_longs.length) {
            throw new RuntimeException("inputs to Hamming distance must be of same length");
        }

        double count = 0;

        for (int i = 0; i < x_as_longs.length; i++) {
            count += distance(x_as_longs[i], y_as_longs[i]);
        }
        return count / x_as_longs.length;
    }

    private int distance(long x, long y) {

        int count = 0;
        while (x > 0 || y > 0) { // keep going until all zeros.
            if (x % 2 != y % 2) {
                count++; // remainders are different
            }
            x = x / 2;  // shift fill with 0
            y = y / 2;  // shift fill with 0
        }
        return count;
    }
}
