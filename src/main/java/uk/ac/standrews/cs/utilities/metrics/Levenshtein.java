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

import com.google.common.base.Preconditions;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

public final class Levenshtein extends StringMetric {

    private final double insertDelete;
    private final double substitute;

    public Levenshtein(double insertDelete, double substitute) {

        Preconditions.checkArgument(insertDelete > 0.0);
        Preconditions.checkArgument(substitute >= 0.0);
        this.insertDelete = insertDelete;
        this.substitute = substitute;
    }

    public Levenshtein() {
        this(1.0, 1.0);
    }

    @Override
    public String getMetricName() {
        return "Levenshtein";
    }

    public double calculateStringDistance(String s, String t) {

        int tLength = t.length();
        int sLength = s.length();
        double[] v0 = new double[tLength + 1];
        double[] v1 = new double[tLength + 1];

        int i;
        for (i = 0; i < v0.length; ++i) {
            v0[i] = (float) i * insertDelete;
        }

        for (i = 0; i < sLength; ++i) {
            v1[0] = (float) (i + 1) * insertDelete;

            for (int j = 0; j < tLength; ++j) {
                v1[j + 1] = min(v1[j] + insertDelete,
                        v0[j + 1] + insertDelete,
                        v0[j] + (s.charAt(i) == t.charAt(j) ? 0.0 : substitute));
            }

            double[] swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return normaliseArbitraryPositiveDistance(v0[tLength]);
    }

    private double min(double a, double b, double c) {

        if (a < b) {
            return Math.min(a, c);
        } else {
            return Math.min(b, c);
        }
    }

    public String toString() {
        return "Levenshtein [insertDelete=" + insertDelete + ", substitute=" + substitute + "]";
    }

    public static void main(String[] a) {

        new Levenshtein().printExamples();
    }
}
