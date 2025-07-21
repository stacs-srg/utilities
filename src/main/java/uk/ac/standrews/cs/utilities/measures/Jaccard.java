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

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

public class Jaccard extends StringMeasure {

    @Override
    public String getMeasureName() {
        return "Jaccard";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {
        return 1 - similarity(x, y);
    }

    public <T> double distance(final Collection<T> x, final Collection<T> y) {
        return 1 - similarity(x, y);
    }

    public double distance(final BitSet x, final BitSet y) {
        return 1 - similarity(x, y);
    }

    private <T> double similarity(final Collection<T> x, final Collection<T> y) {

        return ((double) (intersection(x, y).size())) / union(x, y).size();
    }

    private double similarity(final String x, final String y) {

        final Set<String> bigrams_a = extractNGrams(topAndTail(clean(x)), 2);
        final Set<String> bigrams_b = extractNGrams(topAndTail(clean(y)), 2);

        return similarity(bigrams_a, bigrams_b);
    }

    private double similarity(final BitSet x, final BitSet y) {

        final BitSet union = x.get(0, x.length());   // and and or are destructive in BitSet
        union.or(y);

        final BitSet intersection = x.get(0, x.length());
        intersection.and(y);

        return ((double) intersection.cardinality()) / union.cardinality();
    }

    public static void main(String[] args) {

        new Jaccard().printExamples();
    }
}
