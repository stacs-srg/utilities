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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
public class MinHashTest {

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

    @Before
    public void setup() {

    }

    @Test
    public void jacquard_minhash_same_as_2grams() {

        String input1 = "The attribute to awe and majesty.";
        String input2 = "The attribute to awe and majesty.";

        Set<String> input1_2grams = MinHash.ngrams(input1,2);
        Set<String> input2_2grams = MinHash.ngrams(input2,2);

        double jacquard_ngrams = Jaccard.jaccard(MinHash.ngrams(input1, 2), MinHash.ngrams(input2, 2));

        double jacquard_minhash = Jaccard.jaccard(
                toCollection(MinHash.createMinHashSignature( input1,MinHash.DEFAULTSIGNATURESIZE, 2 )),
                toCollection(MinHash.createMinHashSignature( input2,MinHash.DEFAULTSIGNATURESIZE, 2)));

        System.out.println( "Diff = " + Math.abs(jacquard_ngrams-jacquard_minhash) );

        assertTrue(Math.abs(jacquard_ngrams-jacquard_minhash) == 0.0 );

    }

    @Test
    public void jacquard_minhash_similar_to_2grams() {

        String input1 = "Jul. O Romeo, Romeo! wherefore art thou Romeo?";
        String input2 = "Jul. O Romeo, 12345! wherefore art thou Romeo?";

        Set<String> input1_2grams = MinHash.ngrams(input1,2);
        Set<String> input2_2grams = MinHash.ngrams(input2,2);

        double jacquard_ngrams = Jaccard.jaccard(MinHash.ngrams(input1, 2), MinHash.ngrams(input2, 2));

        double jacquard_minhash = Jaccard.jaccard(
                toCollection(MinHash.createMinHashSignature( input1,20, 2 )),
                toCollection(MinHash.createMinHashSignature( input2,20, 2 )));

        System.out.println( jacquard_ngrams + " " + jacquard_minhash+ " Diff = " + Math.abs(jacquard_ngrams-jacquard_minhash) );

        assertTrue(Math.abs(jacquard_ngrams-jacquard_minhash) < 0.2 );

    }

    @Test
    public void jacquard_minhash__similar_to_2grams_diff_strings() {

        String input1 = "Jul. O Romeo, Romeo! wherefore art thou Romeo?";
        String input2 = "This is a totally different string than above!";

        Set<String> input1_2grams = MinHash.ngrams(input1,2);
        Set<String> input2_2grams = MinHash.ngrams(input2,2);

        double jacquard_ngrams = Jaccard.jaccard(MinHash.ngrams(input1, 2), MinHash.ngrams(input2, 2));

        double jacquard_minhash = Jaccard.jaccard(
                toCollection(MinHash.createMinHashSignature( input1,20, 2 )),
                toCollection(MinHash.createMinHashSignature( input2,20, 2 )));

        System.out.println( jacquard_ngrams + " " + jacquard_minhash+ " Diff = " + Math.abs(jacquard_ngrams-jacquard_minhash) );

        assertTrue(Math.abs(jacquard_ngrams-jacquard_minhash) < 0.2 );

    }

}
