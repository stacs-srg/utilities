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
package uk.ac.standrews.cs.utilities.measures.coreConcepts;

import java.util.*;

public abstract class StringMeasure extends Measure<String> {

    /**
     * Adds a character to the front and end of the string - ensures that even empty strings contain 1 2-gram.
     *
     * @param s - a string to be encapsulated
     * @return an a string encapsulated with ^ and $
     */
    protected static String topAndTail(String s) {
        return "^" + s + "$";
    }

    protected static double min(final double a, final double b, final double c) {

        return a < b ? Math.min(a, c): Math.min(b, c);
    }

    protected static double max(final double a, final double b, final double c) {

        return a > b ? Math.max(a, c): Math.max(b, c);
    }

    protected static String clean(String s) {
        return s.replaceAll("\uFFFD", "");
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
        System.out.println(getMeasureName() + ":");
        System.out.println("normalised: " + maxDistanceIsOne());
        System.out.println();

        System.out.println("empty/empty: " + distance("", ""));
        System.out.println("empty/a: " + distance("", "a"));
        System.out.println("empty/cat: " + distance("", "cat"));
        System.out.println("cat/empty: " + distance("cat", ""));
        System.out.println("a/a: " + distance("a", "a"));
        System.out.println("cat/cat: " + distance("cat", "cat"));
        System.out.println("mclauchlan/mclauchlan: " + distance("mclauchlan", "mclauchlan"));
        System.out.println("caterpillar/caterpillar: " + distance("caterpillar", "caterpillar"));
        System.out.println("pillar/caterpillar: " + distance("pillar", "caterpillar"));
        System.out.println("bat/cat: " + distance("bat", "cat"));
        System.out.println("cat/bat: " + distance("cat", "bat"));
        System.out.println("cat/cart: " + distance("cat", "cart"));
        System.out.println("cat/zoo: " + distance("cat", "zoo"));
        System.out.println("cat/caterpillar: " + distance("cat", "caterpillar"));
        System.out.println("caterpillar/cat: " + distance("caterpillar", "cat"));
        System.out.println("carterpillar/caterpillar: " + distance("carterpillar", "caterpillar"));
        System.out.println("n/zoological: " + distance("n", "zoological"));
        System.out.println("aardvark/zoological: " + distance("aardvark", "zoological"));
        System.out.println("KRISTINA KATARINA/KATARINA KRISTINA: " + distance("KRISTINA KATARINA", "KATARINA KRISTINA"));
        System.out.println("abcdefghijklmnopqrstuvwxyz/zyxwvutsrqponmlkjihgfedcba: " + distance("abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba"));

        String s1 = "NILSSJÖBERGINGRIDERSDOTTER------------";
        String s2 = "JONJONSSON LENBERGINGRID GRETANILSDR20051835----";
        String s3 = "JONJONSSON LENBERGINGRID GRETANILSDOTTER20051835----";

        System.out.println("s1/s2: " + s1 + "/" + s2 + ": " + distance(s1, s2));
        System.out.println("s2/s3: " + s2 + "/" + s3 + ": " + distance(s2, s3));
        System.out.println("s1/s3: " + s1 + "/" + s3 + ": " + distance(s1, s3));
    }
}
