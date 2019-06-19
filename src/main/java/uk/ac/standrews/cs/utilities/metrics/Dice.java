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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.Set;

public class Dice extends StringMetric {

    @Override
    public String getMetricName() {
        return "Dice";
    }

    @Override
    public double calculateStringDistance(String x, String y) {

        return 1.0 - similarity(x, y);
    }

    private double similarity(String A, String B) {

        Set<String> agrams = extractNGrams(topAndTail(A), 2);
        Set<String> bgrams = extractNGrams(topAndTail(B), 2);

        if (agrams.isEmpty() && bgrams.isEmpty()) {
            return 1.0;
        } else {
            return !agrams.isEmpty() && !bgrams.isEmpty() ? 2.0 * intersection(agrams, bgrams).size() / (double) (agrams.size() + bgrams.size()) : 0.0;
        }
    }

    public static void main(String[] a) {

        new Dice().printExamples();
    }
}
