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
 *
 */
package uk.ac.standrews.cs.utilities.metrics.coreConcepts;

public interface NamedMetric<T> extends Metric<T> {

    String getMetricName();

    static double checkNullAndEmpty(String A, String B) {

        if (A.equals(B)) {
            return 1.0;
        }
        if (A.isEmpty() || B.isEmpty()) {
            return 0.0;
        }
        return -1;
    }

    /**
     * Adds a character to the front and end of the string - ensures that even empty strings contain 1 2-gram.
     *
     * @param x - a string to be encapsulated
     * @return an a string encapsulated with ^ and $
     */
    static String topAndTail(String x) {
        return "^" + x + "$";
    }

    static void printExamples(final NamedMetric<String> metric) {

        System.out.println(metric.getMetricName());

        System.out.println("empty/empty: " + metric.distance("", ""));
        System.out.println("empty/a: " + metric.distance("", "a"));
        System.out.println("a/a: " + metric.distance("a", "a"));
        System.out.println("empty/cat: " + metric.distance("", "cat"));
        System.out.println("cat/empty: " + metric.distance("cat", ""));
        System.out.println("cat/cat: " + metric.distance("cat", "cat"));
        System.out.println("mclauchlan/mclauchlan: " + metric.distance("mclauchlan", "mclauchlan"));
        System.out.println("pillar/caterpillar: " + metric.distance("pillar", "caterpillar"));  //  6/11 correct
        System.out.println("bat/cat: " + metric.distance("bat", "cat"));
        System.out.println("cat/bat: " + metric.distance("cat", "bat"));
        System.out.println("cat/cart: " + metric.distance("cat", "cart"));
        System.out.println("cat/caterpillar: " + metric.distance("cat", "caterpillar"));
        System.out.println("carterpillar/caterpillar: " + metric.distance("carterpillar", "caterpillar"));
        System.out.println("caterpillar/cat: " + metric.distance("caterpillar", "cat"));
        System.out.println("cat/zoo: " + metric.distance("cat", "zoo"));
        System.out.println("n/zoological: " + metric.distance("n", "zoological"));
        System.out.println("abcdefghijklmnopqrstuvwxyz/zyxwvutsrqponmlkjihgfedcba: " + metric.distance("abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba"));

        String s1 = "NILSSJÖBERGINGRIDERSDOTTER------------";
        String s2 = "JONJONSSON LENBERGINGRID GRETANILSDR20051835----";
        String s3 = "JONJONSSON LENBERGINGRID GRETANILSDOTTER20051835----";

        System.out.println("s1/s2: " + s1 + "/" + s2 + ": " + metric.distance(s1, s2));
        System.out.println("s2/s3: " + s2 + "/" + s3 + ": " + metric.distance(s2, s3));
        System.out.println("s1/s3: " + s1 + "/" + s3 + ": " + metric.distance(s1, s3));
    }
}
