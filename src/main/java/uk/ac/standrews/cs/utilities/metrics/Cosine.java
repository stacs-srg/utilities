/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cosine implements Metric<String> {

    public double distance(String x, String y) {
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
        Iterator<QgramDistribution> q_iter = q.getIterator();

        double dot_product = 0.0d;
        double p_squared = 0.0d;
        double q_squared = 0.0d;


        while (p_iter.hasNext()) {

            QgramDistribution pi = p_iter.next();
            p_squared = p_squared + pi.count * pi.count;
            QgramDistribution qi;
            if( q_iter.hasNext() ) {
                qi = q_iter.next();
                q_squared = q_squared + qi.count * qi.count;
            } else {  // at the end of q
                break;
            }
            if( pi.compareTo(qi) < 0 ) {
                // qi is missing - just keep going - 0 contribution to dot_product.
            } else {
                // qi > pi - have extra values in q_distro - need to eat them up - these all contribute zero to the calculation - p[i].q[i]
                while( qi != null && pi.compareTo(qi) > 0 ) {

                    try {
                        qi = q_iter.next();
                        q_squared = q_squared + qi.count * qi.count;
                    } catch (NoSuchElementException e) { // at the end of q
                        qi = null;
                    }
                }
            }
            if (pi.equals(qi)) {     // keys are the same so do product calculation
                dot_product += pi.count * qi.count;
            }
        }

        return 1 - dot_product / ( Math.sqrt(p_squared) * Math.sqrt(q_squared) );   // distance not similarity
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
        System.out.println("mclauchlan/mclauchlan: " + cos.distance("mclauchlan", "mclauchlan"));
        System.out.println("pillar/caterpillar: " + cos.distance("pillar", "caterpillar"));  //  6/11 correct
        System.out.println("bat/cat: " + cos.distance("bat", "cat"));
        System.out.println("cat/cart: " + cos.distance("cat", "cart"));
        System.out.println("cat/caterpillar: " + cos.distance("cat", "caterpillar"));
        System.out.println("cat/zoo: " + cos.distance("cat", "zoo"));
        System.out.println("n/zoological: " + cos.distance("n", "zoological"));

    }

}
