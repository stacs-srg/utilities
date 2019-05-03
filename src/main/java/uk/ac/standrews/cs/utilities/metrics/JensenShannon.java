/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
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

/**
 * @author Richard Connor richard.connor@strath.ac.uk
 * Implements Structural Entropic Distance
 *
 */
package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class JensenShannon extends SED {

    public JensenShannon(int maxCharVal) {
        super(maxCharVal);
    }

    @Override
    public String getMetricName() {
        return "JensenShannon";
    }

    public double distance(String x, String y) {

        double check = NamedMetric.checkNullAndEmpty(x, y);
        if (check != -1) return 1 - check;

        SparseProbabilityArray s1 = stringToSparseArray(x);
        SparseProbabilityArray s2 = stringToSparseArray(y);
        return SparseProbabilityArray.JSDistance(s1, s2);
    }

    public static void main(String[] a) {

        NamedMetric.printExamples(new JensenShannon(255));
    }
}