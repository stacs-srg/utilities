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

import java.util.*;

/**
 * Created by al on 04/09/2017.
 */
public class MinHash {

    private final static int someprime = 1190699; // this is a fine prime to use from https://primes.utm.edu/lists/small/small2.html
    private final static int bigprime = 2147483647; // the biggest 32 bit prime integer?
    private final static int anotherprime = 16777619;

    public final static int DEFAULTSIGNATURESIZE = 50; // TODO No idea what size to make this!
    private static final int DEFAULTBANDSIZE = 5;

    private int signature_size = DEFAULTSIGNATURESIZE;
    private int band_size = DEFAULTBANDSIZE;

    private HashMap<Band, Set<String>> lsh_map = new HashMap<>();

    public MinHash(int signature_size, int band_size) {
        this.signature_size = signature_size;
        this.band_size = band_size;
    }

    public MinHash() {
        this(DEFAULTSIGNATURESIZE, DEFAULTBANDSIZE);
    }

    /**
     * Create all the ngrams of length n of the source
     *
     * @param n      - the length of the ngrams that are required.
     * @param source - the string from which the ngrams are created
     * @return the ngrams of the input string
     */
    static Set<String> ngrams(String source, int n) {
        HashSet<String> ngrams = new HashSet<String>();
        for (int i = 0; i < source.length() - n + 1; i++)
            ngrams.add(source.substring(i, i + n));
        return ngrams;
    }

    /**
     * The hash function used to calculate minhash - stolen from this post:
     * from http://stackoverflow.com/questions/263400/what-is-the-best-algorithm-for-an-overridden-system-object-gethashcode
     * By altering the params can create a family of different unique hash functions.
     *
     * @param inputData - the object for which we are creating a hash
     * @param seedOne   - the first seed used to generate a particular unique hash
     * @param seedTwo   - the second seed used to generate a particular unique hash
     * @return the hash of the inputData supplied to the method.
     */
    private static int hashFunction(Object inputData, Integer seedOne, Integer seedTwo) {
        int hash = bigprime * anotherprime ^ seedOne.hashCode();
        hash = hash * anotherprime ^ seedTwo.hashCode();
        hash = hash * anotherprime ^ inputData.hashCode();
        return hash;
    }

    /**
     * Creates a minhash signature for the source string
     *
     * @param src      the string from which a minhash signature will be created
     * @param sig_size the size of the signature created
     * @return the minhash signature
     */
    public static int[] createMinHashSignature(String src, int sig_size) {
//        String stripped = src.replaceAll( "\\s","" ). // replace all whitespace with nothing.
//                replaceAll("[.,~{}()!\\\\]", ""); // replace other punctuation stops with nothing
        Set<String> set_ngrams = ngrams(src, 2);

        //Create a min hash array initialized to all int max values
        int[] signature = new int[sig_size];
        for (int index = 0; index < sig_size; index++) {
            signature[index] = Integer.MAX_VALUE;
        }

        for (String token : set_ngrams) { // for each of the ngrams...

            Iterator<Integer> seeds = new Random(someprime).ints().iterator(); // a deterministic feed of random numbers - use the same hashes every each time

            for (int index = 0; index < sig_size; index++) { // do the hashing for each of the hash functions

                int currentHashValue = hashFunction(token, seeds.next(), seeds.next()); // do the hash

                if (currentHashValue < signature[index]) { //Only retain the minimum value and put into signature
                    signature[index] = currentHashValue;
                }
            }
        }
        return signature;
    }


    public void addMinHashToLSHashMap(String source) {
        int[] minHashSignature = createMinHashSignature(source, signature_size);

//      System.out.println( "Source = " + source );
//      System.out.println( "Signature = " + minHashSignature );

        for (int band_number = 0; band_number * band_size < minHashSignature.length; band_number++) {

            Band b = new Band(minHashSignature, band_number, band_size);

            Set<String> entries = lsh_map.get(b);
            if (entries == null) {
                entries = new HashSet<String>();   // create a new set of entries,
                entries.add(source);               // add the string to it,
                lsh_map.put(b, entries);         // and add the new entry to the map.
            } else {
                entries.add(source); // add the string to the existing map bucket.
            }
        }
    }

    public Set<String> getClosest(String source) {

        Set<String> result = new HashSet<>();

        int[] minHashSignature = createMinHashSignature(source, signature_size);

        for (int band_number = 0; band_number * band_size < minHashSignature.length; band_number++) {

            Band b = new Band(minHashSignature, band_number, band_size);
            Set<String> found = lsh_map.get(b);
            if (found != null) {
                result.addAll(found);
            }
        }
        return result;
    }

    public void printMap() {

        for( Map.Entry<Band, Set<String>> me : lsh_map.entrySet() ) {
            Set<String> entries = me.getValue();
            System.out.println( entries.size() );

        }
    }

}