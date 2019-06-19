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
package uk.ac.standrews.cs.utilities.metrics.coreConcepts;

import java.util.HashSet;
import java.util.Set;

public abstract class StringMetric extends Metric<String> {

    protected final double calculateDistance(String x, String y) {

        if (x.equals(y)) {
            return 0.0;
        }

        if (x.isEmpty() || y.isEmpty()) {
            return 1.0;
        }

        return calculateStringDistance(x, y);
    }

    protected abstract double calculateStringDistance(String x, String y);

    /**
     * Adds a character to the front and end of the string - ensures that even empty strings contain 1 2-gram.
     *
     * @param x - a string to be encapsulated
     * @return an a string encapsulated with ^ and $
     */
    protected static String topAndTail(String x) {
        return "^" + x + "$";
    }

    /**
     * Creates all the ngrams of length n of the source
     *
     * @param n      - the length of the ngrams that are required.
     * @param source - the string from which the ngrams are created
     * @return the ngrams of the input string
     */
    public static Set<String> extractNGrams(String source, int n) {

        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i < source.length() - n + 1; i++)
            ngrams.add(source.substring(i, i + n));
        return ngrams;
    }

    public void printExamples() {

        System.out.println();
        System.out.println(getMetricName() + ":");
        System.out.println();

        System.out.println("empty/empty: " + distance("", ""));
        System.out.println("empty/a: " + distance("", "a"));
        System.out.println("a/a: " + distance("a", "a"));
        System.out.println("empty/cat: " + distance("", "cat"));
        System.out.println("cat/empty: " + distance("cat", ""));
        System.out.println("cat/cat: " + distance("cat", "cat"));
        System.out.println("mclauchlan/mclauchlan: " + distance("mclauchlan", "mclauchlan"));
        System.out.println("pillar/caterpillar: " + distance("pillar", "caterpillar"));  //  6/11 correct
        System.out.println("bat/cat: " + distance("bat", "cat"));
        System.out.println("cat/bat: " + distance("cat", "bat"));
        System.out.println("cat/cart: " + distance("cat", "cart"));
        System.out.println("cat/caterpillar: " + distance("cat", "caterpillar"));
        System.out.println("carterpillar/caterpillar: " + distance("carterpillar", "caterpillar"));
        System.out.println("caterpillar/cat: " + distance("caterpillar", "cat"));
        System.out.println("cat/zoo: " + distance("cat", "zoo"));
        System.out.println("n/zoological: " + distance("n", "zoological"));
        System.out.println("abcdefghijklmnopqrstuvwxyz/zyxwvutsrqponmlkjihgfedcba: " + distance("abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba"));

        String s1 = "NILSSJÖBERGINGRIDERSDOTTER------------";
        String s2 = "JONJONSSON LENBERGINGRID GRETANILSDR20051835----";
        String s3 = "JONJONSSON LENBERGINGRID GRETANILSDOTTER20051835----";

        System.out.println("s1/s2: " + s1 + "/" + s2 + ": " + distance(s1, s2));
        System.out.println("s2/s3: " + s2 + "/" + s3 + ": " + distance(s2, s3));
        System.out.println("s1/s3: " + s1 + "/" + s3 + ": " + distance(s1, s3));
    }
}
