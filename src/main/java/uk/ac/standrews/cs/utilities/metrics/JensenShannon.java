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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class JensenShannon extends SED implements NamedMetric<String> {

    public JensenShannon(int maxCharVal) {
        super(maxCharVal);
    }

    public double distance(String x, String y) {

        double check = CheckValues.checkNullAndEmpty(x, y);
        if (check != -1) return 1 - check;

        SparseProbabilityArray s1 = stringToSparseArray(x);
        SparseProbabilityArray s2 = stringToSparseArray(y);
        return SparseProbabilityArray.JSDistance(s1, s2);
    }

    @Override
    public String getMetricName() {
        return "JensenShannon";
    }

    public static void main(String[] a) {
        JensenShannon js2 = new JensenShannon(255);

        System.out.println("JS2:" );

        System.out.println("empty/empty: " + js2.distance("", ""));
        System.out.println("empty/cat: " + js2.distance("", "cat"));
        System.out.println("cat/cat: " + js2.distance("cat", "cat"));
        System.out.println( "cat/zoo: " + js2.distance( "cat", "zoo" ) );
        System.out.println( "mclauchlan/mclauchlan: " + js2.distance( "mclauchlan", "mclauchlan" ) );
        System.out.println( "pillar/caterpillar: " + js2.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "cat/bat: " + js2.distance( "cat", "bat" ) );
        System.out.println( "bat/cat: " + js2.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + js2.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " + js2.distance( "cat", "caterpillar" ) );
        System.out.println( "n/zoological: " + js2.distance( "n", "zoological" ) );
        System.out.println( "a/hi: " + js2.distance( "a", "hej" ));
    }
}