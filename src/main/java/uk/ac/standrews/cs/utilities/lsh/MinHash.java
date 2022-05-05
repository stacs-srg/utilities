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

import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.*;

/**
 * Implementation of MinHash
 * Created by al on 04/09/2017.
 */
public class MinHash<Data> {

    private final static int someprime = 1190699; // this is a fine prime to use from https://primes.utm.edu/lists/small/small2.html
    private final static int bigprime = 2147483647; // the biggest 32 bit prime integer?
    private final static int anotherprime = 16777619;

    protected int shingle_size;
    protected int signature_size;
    protected int band_size;

    protected HashMap<Band, Set<Data>> lsh_map = new HashMap<>();

    /**
     * Create a min hash map using default values of specified sizes
     * @param num_bands - the number of bands to use in the final hash
     * @param band_size - the size of the bands to use when placing into the min hash
     */
    public MinHash(int shingle_size, int num_bands, int band_size) {
        this.shingle_size = shingle_size;
        this.signature_size = num_bands * band_size;
        this.band_size = band_size;
    }

    public MinHashStructure showStructure() {
        return new MinHashStructure( this );
    }

    /**
     * The hash function used to calculate minhash
     * By altering the params can create a family of different unique hash functions.
     *
     * @param inputData - the object for which we are creating a hash
     * @param seedOne   - the first seed used to generate a particular unique hash
     * @param seedTwo   - the second seed used to generate a particular unique hash
     * @return the hash of the inputData supplied to the method.
     */
    private static int hashFunction(Object inputData, int seedOne, int seedTwo) {

        // TODO is this intended to overflow?
        int hash = bigprime * anotherprime ^ seedOne;
        hash = hash * anotherprime ^ seedTwo;
        hash = hash * anotherprime ^ inputData.hashCode();
        return hash;
    }

    /**
     * Creates a minhash signature for the source string
     * @param src      the string from which a minhash signature will be created
     * @param sig_size the size of the signature created
     * @return the minhash signature
     */
    public static Integer[] createMinHashSignature(String src, int sig_size, int shingle_size) {

        Set<String> set_ngrams = StringMeasure.extractNGrams(src, shingle_size);

        // Create a min hash array initialized to all int max values
        Integer[] signature = new Integer[sig_size];
        for (int index = 0; index < sig_size; index++) {
            signature[index] = Integer.MAX_VALUE;
        }

        for (String token : set_ngrams) { // for each of the ngrams...

            Iterator<Integer> seeds = new Random(someprime).ints().iterator(); // a deterministic feed of random numbers - use the same hashes every each time

            for (int index = 0; index < sig_size; index++) { // do the hashing for each of the hash functions

                int currentHashValue = hashFunction(token, seeds.next(), seeds.next()); // do the hash

                if (currentHashValue < signature[index]) { //Only retain the minimum value and addHint into signature
                    signature[index] = currentHashValue;
                }
            }
        }
        return signature;
    }

    /**
     * Put the value into a LSH Map with key key.
     * @param key - the key to which the data should be mapped
     * @param value - the value to addHint in the map.
     */
    public void put(String key, Data value) {

        Integer[] minHashSignature = createMinHashSignature(key, signature_size, shingle_size);

        for (int band_number = 0; band_number * band_size < minHashSignature.length; band_number++) {

            Band b = new Band(minHashSignature, band_number, band_size);

            Set<Data> entries = lsh_map.get(b);
            if (entries == null) {
                entries = new HashSet<>();   // create a new set of entries,
                entries.add(value);               // add_worker the string to it,
                lsh_map.put(b,entries);         // and add_worker the new entry to the map.
            } else {
                entries.add(value); // add_worker the string to the existing map bucket.
            }
        }
    }

    /**
     * @param key - the key of the data to be searched.
     * @return the set of data that are mapped by the key
     */
    public Set<Data> getClosest(String key) {

        Set<Data> result = new HashSet<>();

        Integer[] minHashSignature = createMinHashSignature(key, signature_size, shingle_size);

        for (int band_number = 0; band_number * band_size < minHashSignature.length; band_number++) {

            Band b = new Band(minHashSignature, band_number, band_size);
            Set<Data> found = lsh_map.get(b);
            if (found != null) {
                result.addAll(found);
            }
        }
        return result;
    }

    /**
     * A debug/diagnostic method to inspect the size of the rhs of the mappings
     */
    public void printMap() {

        for( Map.Entry<Band, Set<Data>> me : lsh_map.entrySet() ) {
            Set<Data> entries = me.getValue();
            System.out.println( entries.size() );
        }
    }
}
