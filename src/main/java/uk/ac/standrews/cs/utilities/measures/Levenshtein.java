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

import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

public final class Levenshtein extends StringMeasure {

    public static final double DEFAULT_INSERT_DELETE_COST = 1.0;
    public static final double DEFAULT_SUBSTITUTE_COST = 1.0;

    private final double insertDeleteCost;
    private final double substituteCost;

    public Levenshtein() {
        this(DEFAULT_INSERT_DELETE_COST, DEFAULT_SUBSTITUTE_COST);
    }

    public Levenshtein(final double insertDeleteCost, final double substituteCost) {

        this.insertDeleteCost = insertDeleteCost;
        this.substituteCost = substituteCost;
    }

    @Override
    public String getMeasureName() {
        return "Levenshtein";
    }

    @Override
    public boolean maxDistanceIsOne() { return false; }

    @Override
    public double calculateDistance(final String x, final String y) {

        final String cleanX = clean(x);
        final String cleanY = clean(y);

        final int lengthX = cleanX.length();
        final int lengthY = cleanY.length();

        double[] v0 = new double[lengthY + 1];
        double[] v1 = new double[lengthY + 1];

        for (int i = 0; i < v0.length; ++i) {
            v0[i] = (double) i * insertDeleteCost;
        }

        for (int i = 0; i < lengthX; ++i) {
            v1[0] = (double) (i + 1) * insertDeleteCost;

            for (int j = 0; j < lengthY; ++j) {
                v1[j + 1] = min(v1[j] + insertDeleteCost,
                        v0[j + 1] + insertDeleteCost,
                        v0[j] + (cleanX.charAt(i) == cleanY.charAt(j) ? 0.0 : substituteCost));
            }

            final double[] swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return v0[lengthY];
    }

    public String toString() {
        return "Levenshtein [insertDelete=" + insertDeleteCost + ", substitute=" + substituteCost + "]";
    }

    public static void main(String[] a) {

        new Levenshtein().printExamples();
    }
}
