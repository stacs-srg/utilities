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

import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static uk.ac.standrews.cs.utilities.metrics.KullbackLeibler.kullbackLeiblerDivergence;

/**
 * Created by al on 06/09/2017.
 */
public class JensenShannonKullbackLeibler implements NamedMetric<String> {

    /**
     *
     * @param x
     * @param y
     * @return the JensenShannonDistance
     * The square root of the Jensen-Shannon divergence is a metric
     */
    public double distance(String x, String y) {

        double check = CheckValues.checkNullAndEmpty(x, y);
        if (check != -1) return check;

        return Math.sqrt(jensenShannonDivergence(x, y));
    }

    @Override
    public String getMetricName() {
        return "JensenShannon";
    }

    /**
     *
     * @param x
     * @param y
     * @return the JensenShannonDistance (from elsewhere):
     * The square root of the Jensen-Shannon divergence is a metric
     */
    public double JensenShannonDistance(double[] x, double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));

        return Math.sqrt(jensenShannonDivergence(x, y));
    }

    /**
     *
     * @param p1
     * @param p2
     * @return  the Jensen-Shannon divergence.
     * The Jensen-Shannon divergence is a symmetric and smoothed version of the Kullback-Leibler divergence
     */
    public double jensenShannonDivergence(double[] p1, double[] p2) {
        assert(p1.length == p2.length);
        double[] average = new double[p1.length];
        for (int i = 0; i < p1.length; i++) {
            average[i] += (p1[i] + p2[i])/2;      // average of the two arrays.
        }
        return (kullbackLeiblerDivergence(p1, average) + kullbackLeiblerDivergence(p2, average))/2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
    }

    public double jensenShannonDivergence(String p1, String p2) {
        SparseDistro p1_distro = new SparseDistro( topAndTail(p1) );
        SparseDistro p2_distro = new SparseDistro( topAndTail(p2) );
        SparseDistro average = null;
        try {
            average = average( p1_distro,p2_distro );
        } catch (Exception e) {
            // this cannot occur since the two distros above were newly created and hence using counts.
            ErrorHandling.hardError( "Unexpected probability based distribution" );
        }

        p1_distro = p1_distro.toProbability();
        p2_distro = p2_distro.toProbability();
        average = average.toProbability();

        double kl1 = kullbackLeiblerDivergence(p1_distro, average);
        double kl2 = kullbackLeiblerDivergence(p2_distro, average);

        return (kl1 + kl2)/2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
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


    private static SparseDistro average(SparseDistro xx, SparseDistro yy) throws Exception {
        if( ! ( xx.is_counting() && yy.is_counting() ) ) {
            throw new Exception( "Can only average counting distributions" );
        }
        SparseDistro ave = new SparseDistro( xx ); // a copy of the first distribution.
        // now average_value in the records from the second
        Iterator<QgramDistribution> yy_iter = yy.getIterator();
        while( yy_iter.hasNext() ) {
            QgramDistribution nxt = yy_iter.next();
            ave.average_value( nxt.key, nxt.count );
        }
        return ave;
    }

    public static Set union(Collection a, Collection b) {
        Set result = new HashSet();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public static Set intersection(Collection a, Collection b) {
        Set result = new HashSet();
        Iterator a_iter = a.iterator();
        while( a_iter.hasNext() ) {
            Object next = a_iter.next();
            if( b.contains( next ) ) {
                result.add( next );
            }
        }
        return result;
    }

    //---------------------------- end of utility code ----------------------------//

    public static void main(String[] args) {

        JensenShannonKullbackLeibler js = new JensenShannonKullbackLeibler();
        KullbackLeibler kl = new KullbackLeibler();

        double[] array1 = new double[]{ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        double[] array2 = new double[]{ 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };

//        System.out.println( kl.kullbackLeiblerDivergence( array1, array1 ) );
//        System.out.println( kl.kullbackLeiblerDivergence( array1, array2 ) );
//        System.out.println( kl.kullbackLeiblerDivergence( array2, array1 ) );

        double[] array3 = new double[]{ 0.05, 0.4, 0.25, 0.3 };
        double[] array4 = new double[]{ 0.4, 0.25, 0.3, 0.05 };

//        System.out.println( kl.kullbackLeiblerDivergence( array1, array4 ) );

//         System.out.println( kl.kullbackLeiblerDivergence( "ABC", "ABC" ) );
//         System.out.println( kl.kullbackLeiblerDivergence( "CDE", "BCDEFCDCDCDCD" ) );
//         System.out.println( kl.kullbackLeiblerDivergence(  "BCDEFCDCDCDCD", "CDE" ) );
//         System.out.println( kl.kullbackLeiblerDivergence( "ABC", "CDE" ) );
//         System.out.println( kl.kullbackLeiblerDivergence( "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ) );


        System.out.println("JS:" );
        System.out.println( "empty/zoo: " + js.distance( "", "zoo" ) );
        System.out.println( "cat/zoo: " + js.distance( "cat", "zoo" ) );
        System.out.println( "mclauchlan/mclauchlan: " + js.distance( "mclauchlan", "mclauchlan" ) );
        System.out.println( "pillar/caterpillar: " + js.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "cat/bat: " + js.distance( "cat", "bat" ) );
        System.out.println( "bat/cat: " + js.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + js.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " + js.distance( "cat", "caterpillar" ) );
        System.out.println( "n/zoological: " + js.distance( "n", "zoological" ) );
        System.out.println( "a/hi: " + js.distance( "a", "hej" ));
    }
}
