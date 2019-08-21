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

import uk.ac.standrews.cs.utilities.metrics.implementation.SparseProbabilityMetric;

/**
 * @author Richard Connor richard.connor@strath.ac.uk
 */
public class JensenShannon extends SparseProbabilityMetric {

    private int CHAR_VAL_UPPER_BOUND = 512;
    public static final String metricName = "JensenShannon";

    public JensenShannon() {}

    public JensenShannon(int CHAR_VAL_UPPER_BOUND) {
        this.CHAR_VAL_UPPER_BOUND = CHAR_VAL_UPPER_BOUND;
    }

    @Override
    public String getMetricName() {
        return metricName;
    }

    public double calculateStringDistance(String x, String y) {

        double k = doCalc(stringToSparseArray(x, CHAR_VAL_UPPER_BOUND), stringToSparseArray(y, CHAR_VAL_UPPER_BOUND));
        return Math.sqrt(Math.max(0, k));
    }

    public static void main(String[] a) {

        new JensenShannon().printExamples();
    }
}