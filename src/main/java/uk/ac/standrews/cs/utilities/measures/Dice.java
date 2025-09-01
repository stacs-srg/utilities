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

import java.util.Set;

public class Dice extends StringMeasure {

    @Override
    public String getMeasureName() {
        return "Dice";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        return 1.0 - similarity(clean(x), clean(y));
    }

    private double similarity(final String x, final String y) {

        final Set<String> x_bigrams = extractNGrams(topAndTail(x), 2);
        final Set<String> y_bigrams = extractNGrams(topAndTail(y), 2);

        return 2.0 * intersection(x_bigrams, y_bigrams).size() / (x_bigrams.size() + y_bigrams.size());
    }

    public static void main(String[] a) {

        new Dice().printExamples();
    }
}
