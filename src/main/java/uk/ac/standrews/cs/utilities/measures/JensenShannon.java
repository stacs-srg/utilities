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
package uk.ac.standrews.cs.utilities.measures;

import uk.ac.standrews.cs.utilities.measures.implementation.SparseProbabilityMeasure;

/**
 * @author Richard Connor richard.connor@strath.ac.uk
 */
public class JensenShannon extends SparseProbabilityMeasure {

    private final int char_val_upper_bound;

    private static final int DEFAULT_UPPER_BOUND = 512;
    private static final double MAX_DISTANCE = 1.0;

    public JensenShannon() {

        this(DEFAULT_UPPER_BOUND);
    }

    public JensenShannon(final int char_val_upper_bound) {
        this.char_val_upper_bound = char_val_upper_bound;
    }

    @Override
    public String getMeasureName() {
        return "JensenShannon";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        if (x.isEmpty() || y.isEmpty()) return MAX_DISTANCE;

        double k = doCalc(stringToSparseArray(x, char_val_upper_bound), stringToSparseArray(y, char_val_upper_bound));
        return Math.sqrt(Math.max(0, k));
    }

    public static void main(String[] a) {

        new JensenShannon().printExamples();
    }
}