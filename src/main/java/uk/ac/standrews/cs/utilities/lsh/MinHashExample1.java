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

package uk.ac.standrews.cs.utilities.lsh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by al on 08/09/2017.
 */
public class MinHashExample1 {

    /**
     * A utility method used for debugging
     * @param arrai - an array to be turned into a colection
     * @return the array as a collection
     */
    private static Collection<Integer> toCollection(int[] arrai ) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for( int i = 0; i < arrai.length; i++) {
            result.add( arrai[i]);
        }
        return result;
    }

    public static void main( String[] args ) {

        String input1 = "The attribute to awe and majesty.";
        String input2 = "But there's but one in all doth hold his place.";

        Set<String> input1_2grams = MinHash.ngrams(input1,2);
        Set<String> input2_2grams = MinHash.ngrams(input2,2);

        int[] input1_minHashSignature = MinHash.createMinHashSignature( input1,MinHash.DEFAULTSIGNATURESIZE, MinHash.DEFAULTSIGNATURESIZE );
        int[] input2_minHashSignature = MinHash.createMinHashSignature( input2,MinHash.DEFAULTSIGNATURESIZE, MinHash.DEFAULTSIGNATURESIZE );

        System.out.println( "Input 1 = " + input1 );
        System.out.println( "Input 2 = " + input2 );

        System.out.println();

        System.out.println( "2grams intersection = " + Jaccard.intersection( input1_2grams, input2_2grams ).size() + ", union = " + Jaccard.union( input1_2grams, input2_2grams ).size() );
        System.out.println( "Jaccard (ngrams) = " + Jaccard.jaccard(input1_2grams,input2_2grams ) );

        System.out.println();

        System.out.println( "minhash intersection = " + Jaccard.intersection( toCollection(input1_minHashSignature), toCollection(input2_minHashSignature) ).size() + ", union = " + Jaccard.union( toCollection(input1_minHashSignature), toCollection(input2_minHashSignature) ).size() );
        System.out.println( "Jaccard (minhash) = " + Jaccard.jaccard(toCollection(input1_minHashSignature), toCollection(input2_minHashSignature)) );

    }
}
