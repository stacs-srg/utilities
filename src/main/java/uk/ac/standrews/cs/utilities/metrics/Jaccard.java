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

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;

/**
 * Created by al on 06/09/2017.
 */
public class Jaccard extends StringMetric {

    @Override
    public String getMetricName() {
        return "Jaccard";
    }

    public double calculateStringDistance(String A, String B) {
        return 1 - similarity(A, B);
    }

    public <T> double distance(Collection<T> A, Collection<T> B) {
        return 1 - similarity(A, B);
    }

    public double distance(BitSet A, BitSet B) {
        return 1 - similarity(A, B);
    }

    private <T> double similarity(Collection<T> A, Collection<T> B) {

        return ((double) (intersection(A, B).size())) / union(A, B).size();
    }

    private double similarity(String A, String B) {

        Set<String> bigrams_a = extractNGrams(topAndTail(A), 2);
        Set<String> bigrams_b = extractNGrams(topAndTail(B), 2);

        return similarity(bigrams_a, bigrams_b);
    }

    private double similarity(BitSet A, BitSet B) {

        BitSet union = A.get(0, A.length());   // and and or are destructive in BitSet
        union.or(B);
        BitSet intersection = A.get(0, A.length());
        intersection.and(B);
        return ((double) intersection.cardinality()) / union.cardinality();
    }

    public static void main(String[] args) {

        new Jaccard().printExamples();
    }
}
