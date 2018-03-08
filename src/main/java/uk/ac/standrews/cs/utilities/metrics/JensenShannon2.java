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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class JensenShannon2 extends SED implements Metric<String> {

    /**
     * @param maxCharVal
     */
    public JensenShannon2(int maxCharVal) {
        super(maxCharVal);
    }

    public double distance(String x, String y) {
        SparseProbabilityArray s1 = stringToSparseArray(x);
        SparseProbabilityArray s2 = stringToSparseArray(y);
        return SparseProbabilityArray.JSDistance(s1, s2);
    }

    @Override
    public String getMetricName() {
        return "JensenShannon2";
    }

    public static void main(String[] a) {
        JensenShannon2 js2 = new JensenShannon2(255);

        System.out.println("JS2:" );

        System.out.println("cat/cat: " + js2.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  js2.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + js2.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + js2.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +js2.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + js2.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + js2.distance( "n", "zoological" ) );
    }

}