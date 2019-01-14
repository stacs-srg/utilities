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

package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.Iterator;

public class Cosine implements StringMetric, NamedMetric<String> {

    public double distance(String x, String y) {

        double check = CheckValues.checkNullAndEmpty(x, y);
        if (check != -1) return check;

        SparseDistro sdx = new SparseDistro(topAndTail(x));
        SparseDistro sdy = new SparseDistro(topAndTail(y));
        return distance(sdx, sdy);
    }

    public double distance(FeatureVector x, FeatureVector y) {
        return distance( new SparseDistro(x),new SparseDistro(y) );
    }

    public double distance(SparseDistro p, SparseDistro q) {

        p = p.toProbability();
        q = q.toProbability();

        Iterator<QgramDistribution> p_iter = p.getIterator();

        double dot_product = 0.0d;

        while (p_iter.hasNext()) {

            QgramDistribution next_qgram = p_iter.next();

            QgramDistribution qi = q.getEntry( next_qgram.key );
            if( qi != null  ) {
                dot_product += next_qgram.count * qi.count;
            }
        }

        double cosine_similarity = dot_product / (p.magnitude() * q.magnitude());
        double angular_distance = 2.0 * Math.acos( cosine_similarity ) / Math.PI;

        return angular_distance;
    }

    @Override
    public String getMetricName() {
        return "Cosine";
    }

    //---------------------------- utility code ----------------------------//

    /**
     * Adds a character to the front and end of the string - ensures that even empty strings contain 1 2-gram.
     * @param x - a string to be encapsulated
     * @return an a string encapsulated with ^ and $
     */
    private static String topAndTail(String x) {
        return "^" + x + "$";
    }

    public static void main(String[] args) {

        Cosine cos = new Cosine();

        System.out.println("Cosine:");
        System.out.println("empty/a: " + cos.distance("", "a"));
        System.out.println("a/a: " + cos.distance("a", "a"));
        System.out.println("mclauchlan/mclauchlan: " + cos.distance("mclauchlan", "mclauchlan"));
        System.out.println("mclauchlan/mclauchln: " + cos.distance("mclauchlan", "mclauchln"));
        System.out.println("pillar/caterpillar: " + cos.distance("pillar", "caterpillar"));  //  6/11 correct
        System.out.println("bat/cat: " + cos.distance("bat", "cat"));
        System.out.println("cat/bat: " + cos.distance("cat", "bat"));        System.out.println("cat/cart: " + cos.distance("cat", "cart"));
        System.out.println("cat/caterpillar: " + cos.distance("cat", "caterpillar"));
        System.out.println("caterpillar/cat: " + cos.distance("caterpillar", "cat"));
        System.out.println("cat/zoo: " + cos.distance("cat", "zoo"));
        System.out.println("n/zoological: " + cos.distance("n", "zoological"));
        System.out.println("abcdefghijklmnopqrstuvwxyz/zyxwvutsrqponmlkjihgfedcba: " + cos.distance("abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba"));

        String s1 = "NILSSJÖBERGINGRIDERSDOTTER------------";
        String s2 = "JONJONSSON LENBERGINGRID GRETANILSDR20051835----";
        String s3 = "JONJONSSON LENBERGINGRID GRETANILSDOTTER20051835----";

        System.out.println("s1/s2: " + s1 + "/" + s2 + ": " + cos.distance(s1, s2));
        System.out.println("s2/s3: " + s2 + "/" + s3 + ": " + cos.distance(s2, s3));
        System.out.println("s1/s3: " + s1 + "/" + s3 + ": " + cos.distance(s1, s3));

        System.out.println( "D(s1,s2)>D(s2,s3)+D(s1,s3): " + ( cos.distance(s1, s2) > cos.distance(s2, s3) + cos.distance(s1, s3) ) );

    }

}
