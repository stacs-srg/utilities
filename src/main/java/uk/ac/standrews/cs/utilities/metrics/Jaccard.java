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

import java.util.*;

/**
 * Created by al on 06/09/2017.
 */
public class Jaccard implements NamedMetric<String> {

    public double distance(Collection A, Collection B ) {
        return 1 - similarity(A,B);
    }

    public double distance(String A, String B ) {
        return 1 - similarity(A,B);
    }

    public double distance(BitSet A, BitSet B ) {
        return 1 - similarity(A,B);
    }

    public double similarity(Collection A, Collection B ) {
        return ( (double) ( intersection( A,B ).size() ) ) / union( A, B ).size();
    }

    public double similarity(String A, String B ) {
        Collection agrams = Shingle.ngrams(A,2);
        Collection bgrams = Shingle.ngrams(B,2);
        return ( (double) ( intersection( agrams,bgrams ).size() ) ) / union( agrams, bgrams ).size();
    }

    public double similarity(BitSet A, BitSet B ) {
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

    public static void main(String[] args) {

        Jaccard jacc = new Jaccard();

        System.out.println("Jaccard:");
        System.out.println("a/a: " + jacc.distance("a", "a"));
        System.out.println("mclauchlan/mclauchlan: " + jacc.distance("mclauchlan", "mclauchlan"));
        System.out.println("pillar/caterpillar: " + jacc.distance("pillar", "caterpillar"));  //  6/11 correct
        System.out.println("bat/cat: " + jacc.distance("bat", "cat"));
        System.out.println("cat/bat: " + jacc.distance("cat", "bat"));        System.out.println("cat/cart: " + jacc.distance("cat", "cart"));
        System.out.println("cat/caterpillar: " + jacc.distance("cat", "caterpillar"));
        System.out.println("caterpillar/cat: " + jacc.distance("caterpillar", "cat"));
        System.out.println("cat/zoo: " + jacc.distance("cat", "zoo"));
        System.out.println("n/zoological: " + jacc.distance("n", "zoological"));
        System.out.println("abcdefghijklmnopqrstuvwxyz/zyxwvutsrqponmlkjihgfedcba: " + jacc.distance("abcdefghijklmnopqrstuvwxyz", "zyxwvutsrqponmlkjihgfedcba"));


    }
}
