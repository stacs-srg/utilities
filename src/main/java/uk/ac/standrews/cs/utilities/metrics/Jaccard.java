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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;

/**
 * Created by al on 06/09/2017.
 */
public class Jaccard implements Metric<String> {

    public double distance(Collection A, Collection B ) {
        return ( (double) ( intersection( A,B ).size() ) ) / union( A, B ).size();
    }

    public double distance(String A, String B ) {
        Collection agrams = Shingle.ngrams(A,2);
        Collection bgrams = Shingle.ngrams(B,2);
        return ( (double) ( intersection( agrams,bgrams ).size() ) ) / union( agrams, bgrams ).size();
    }

    public double distance(BitSet A, BitSet B ) {
        BitSet union = A.get(0,A.length());   // and and or are destructive in BitSet
        union.or(B);
        BitSet intersection = A.get(0,A.length());
        intersection.and(B);
        return ( (double) intersection.cardinality() ) / union.cardinality();
    }


    @Override
    public String getMetricName() {
        return "Jaccard";
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
}
