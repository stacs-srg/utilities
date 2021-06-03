/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
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

import org.junit.Test;
import uk.ac.standrews.cs.utilities.metrics.Jaccard;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
public class MinHashTest {

    private final static int DEFAULTSIGNATURESIZE_TEST = 50; // Arbitrary number

    Jaccard jaccard = new Jaccard();

    @Test
    public void jaccard_minhash_same_as_2grams() {

        String input1 = "The attribute to awe and majesty.";
        String input2 = "The attribute to awe and majesty.";

        double jaccard_ngrams = jaccard.distance(StringMetric.extractNGrams(input1, 2), StringMetric.extractNGrams(input2, 2));

        double jaccard_minhash = jaccard.distance(
                Arrays.asList(MinHash.createMinHashSignature(input1, DEFAULTSIGNATURESIZE_TEST, 2)),
                Arrays.asList(MinHash.createMinHashSignature(input2, DEFAULTSIGNATURESIZE_TEST, 2)));

        assertEquals(0.0, Math.abs(jaccard_ngrams - jaccard_minhash));
    }

    @Test
    public void jaccard_minhash_similar_to_2grams() {

        String input1 = "Jul. O Romeo, Romeo! wherefore art thou Romeo?";
        String input2 = "Jul. O Romeo, 12345! wherefore art thou Romeo?";

        double jaccard_ngrams = jaccard.distance(StringMetric.extractNGrams(input1, 2), StringMetric.extractNGrams(input2, 2));

        double jaccard_minhash = jaccard.distance(
                Arrays.asList(MinHash.createMinHashSignature(input1, 20, 2)),
                Arrays.asList(MinHash.createMinHashSignature(input2, 20, 2)));

        assertTrue(Math.abs(jaccard_ngrams - jaccard_minhash) < 0.2);
    }

    @Test
    public void jaccard_minhash__similar_to_2grams_diff_strings() {

        String input1 = "Jul. O Romeo, Romeo! wherefore art thou Romeo?";
        String input2 = "This is a totally different string than above!";

        double jaccard_ngrams = jaccard.distance(StringMetric.extractNGrams(input1, 2), StringMetric.extractNGrams(input2, 2));

        double jaccard_minhash = jaccard.distance(
                Arrays.asList(MinHash.createMinHashSignature(input1, 20, 2)),
                Arrays.asList(MinHash.createMinHashSignature(input2, 20, 2)));

        assertTrue(Math.abs(jaccard_ngrams - jaccard_minhash) < 0.2);
    }
}
