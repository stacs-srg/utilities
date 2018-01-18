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

package uk.ac.standrews.cs.utilities.metrics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Shingle {
    /**
     * Create all the ngrams of length n of the source
     *
     * @param n      - the length of the ngrams that are required.
     * @param source - the string from which the ngrams are created
     * @return the ngrams of the input string
     */
    public static Set<String> ngrams(String source, int n) {
        HashSet<String> ngrams = new HashSet<String>();
        for (int i = 0; i < source.length() - n + 1; i++)
            ngrams.add(source.substring(i, i + n));
        return ngrams;
    }

    public static Iterator<String> ngramIterator(String source, int n) {
        return new Iterator<String>() {

            int position = 0;
            int number_grams = source.length() - n + 1; // number of ngrams possible.

            @Override
            public boolean hasNext() {
                return position < number_grams;
            }

            @Override
            public String next() {
                try {
                    return source.substring(position, position++ + n);
                } catch( IndexOutOfBoundsException e ) {
                    return null;
                }
            }
        };
    }

}
