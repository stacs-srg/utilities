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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class KullbackLeibler {

    /**
     * CODE FROM: package cc.mallet.util
     * Down to /*-*-*...
     *
     * This software is provided under the terms of the Common Public License,
     * version 1.0, as published by http://www.opensource.org.  For further
     * information, see the file `LICENSE' included with this distribution.
     *
     * @author <a href="mailto:casutton@cs.umass.edu">Charles Sutton</a>
     * @version $Id: ArrayUtils.java,v 1.1 2007/10/22 21:37:40 mccallum
     *
     * Error in that version of the code - wrong log used - corrected.
     *
     * McCallum, Andrew Kachites.  "MALLET: A Machine Learning for Language Toolkit."
     * http://www.cs.umass.edu/~mccallum/mallet.
     * now at: http://mallet.cs.umass.edu
     * 2002.
     **/
    /**
     * @param p
     * @param q
     * @return the Kullback–Leibler divergence
     * The Kullback–Leibler divergence (also called relative entropy) is a
     * measure of how one probability distribution diverges from a second probability distribution.
     * See https://en.wikipedia.org/wiki/Kullback–Leibler_divergence
     * CODE FROM: package cc.mallet.util (see comment above).
     *
     * KL measures the expected number of extra bits required to code samples from P when using a code based on Q,
     * rather than using a code based on P. Typically P represents the "true" distribution of data, observations,
     * or a precisely calculated theoretical distribution. The measure Q typically represents a theory, model, description, or approximation of P.
     *
     * Code performs Sigma(over i) of ( p(i) log( p(i) / q(i) )
     * That is: Sigma(over i) of rel frequency of pi * log( rel frequency of pi / rel frequency of qi )
     */
    public static double kullbackLeiblerDivergence(double[] p, double[] q) {
        assert p.length == q.length;
        assert  sum( p ) == 1.0d;
        assert  sum( q ) == 1.0d;
        double divergence = 0.0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] == 0) {
                continue;  // no point in looking at q since product is zero and log 0 is undefined - jump out to next iteration.
            }
            if (q[i] == 0) {
                return Double.POSITIVE_INFINITY; // when p ̸= 0 but q = 0, KL(p||q) is defined as ∞
            }
            divergence += p[i] * Math.log10(p[i] / q[i]); // rel frequency of pi * log( rel frequency of pi / rel frequency of qi )
        }
        return divergence;
    }


    /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- end of MALLET code -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/


    /**
     * Implements the kullbackLeiblerDivergence described in the method of the same name over sparse representations.
     * @param x
     * @param y
     * @return the Kullback–Leibler divergence
     * 0 indicates that we can expect similar, if not the same, behavior of two different distributions,
     * while a Kullback–Leibler divergence of 1 indicates that the expectation of seeing the second given the first approaches zero
     */
    public static double kullbackLeiblerDivergence( String x, String y ) {
        return kullbackLeiblerDivergence( new SparseDistro( topAndTail( x ) ).toProbability(),new SparseDistro(topAndTail( y )).toProbability());
    }

    /**
     * Implements the kullbackLeiblerDivergence described in the method of the same name over sparse representations.
     * @return the Kullback–Leibler divergence
     */
    public static double kullbackLeiblerDivergence22(SparseDistro distro_p, SparseDistro distro_q ) {

        Iterator<QgramDistribution> p_iter = distro_p.getIterator();
        Iterator<QgramDistribution> q_iter = distro_q.getIterator();

        if (distro_p.size() == 0 || distro_q.size() == 0) {    // ensures that there is at least 1 element in each - makes code easier.
            return 0.0;
        }

        double divergence = 0.0;

        while (p_iter.hasNext()) {

            QgramDistribution pi = p_iter.next();
            QgramDistribution qi;
            if( q_iter.hasNext() ) {
                qi = q_iter.next();
            } else {  // at the end of q
                return Double.POSITIVE_INFINITY; // when p ̸= 0 but q = 0, KL(p||q) is defined as ∞
            }
            if( pi.compareTo(qi) < 0 ) {
                // qi is missing - return infinity and we are done
                return Double.POSITIVE_INFINITY;
            } else {
                // qi > pi - have extra values in q_distro - need to eat them up - these all contribute zero to the comparison - p[i] == 0 and q[i] != 0.
                while( qi != null && pi.compareTo(qi) > 0 ) {

                    try {
                        qi = q_iter.next();
                    } catch (NoSuchElementException e) { // at the end of q
                        qi = null;
                    }
                }
            }
            if (pi.equals(qi)) {     // keys are the same so do comparison
                divergence += pi.count * log2(pi.count / qi.count); //**** This is wrong.
            }

        }
        // At the end of the loop if we had run out of qs we would have returned POSITIVE_INFINITY
        // We have exhausted p_iter in while loop.
        // May have unmatched elements left in distro_q but these don't matter since if (p[i] == 0) then the contribution is 0.

        return divergence;
    }

    /**
     * Implements the kullbackLeiblerDivergence described in the method of the same name over sparse representations.
     * @return the Kullback–Leibler divergence
     */
    public static double kullbackLeiblerDivergence(SparseDistro distro_p, SparseDistro distro_q ) {

        Iterator<QgramDistribution> p_iter = distro_p.getIterator();

        double divergence = 0.0;

        while (p_iter.hasNext()) {

            QgramDistribution pi = p_iter.next();
            QgramDistribution qi = distro_q.getEntry(pi.key);

            if( qi == null ) {
                // no coresponding bigram in q
                return Double.POSITIVE_INFINITY;
            }
            // keys are the same so do comparison
            divergence += pi.count * log2(pi.count / qi.count); //**** This is wrong!   // is it ? Math.log (natural).
        }
        return divergence;
    }

    private static double log2 = Math.log( 2 );

    // Logb x = Loga x/Loga b
    private static double log2( double x ) {
        return Math.log( x ) / log2;
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

    private static double sum(double[] p) {
        double total = 0.0d;
        for( int i = 0; i < p.length; i++ ) {
            total+= p[i];
        }
        return total;
    }
}
