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
 * Implements Structural Entropic Distance
 */
public class SED extends SparseProbabilityMeasure {

    private final int char_val_upper_bound;

    private static final int DEFAULT_CHAR_VAL_UPPER_BOUND = 512;
    private static final double MAX_DISTANCE = 1.0;

    public SED() {
        this(DEFAULT_CHAR_VAL_UPPER_BOUND);
    }

    public SED(int char_val_upper_bound) {
        this.char_val_upper_bound = char_val_upper_bound;
    }

    @Override
    public String getMeasureName() {
        return "SED";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {
        final String cleanX = clean(x);
        final String cleanY = clean(y);

        if (cleanX.isEmpty() || cleanY.isEmpty()) return MAX_DISTANCE;

        final double k = doCalc(stringToSparseArray(cleanX, char_val_upper_bound), stringToSparseArray(cleanY, char_val_upper_bound));
        return Math.pow(Math.pow(2, Math.max(0, k)) - 1, 0.486); // TODO magic number
    }

    public static void main(String[] a) {
        new SED().printExamples();
    }
}