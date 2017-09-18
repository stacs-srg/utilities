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

/**
 * Created by al on 07/09/2017 at the University of Liverpool.
 */
public class Band {

    private final int bigprime = 2147483647;
    private int the_hash;

    Band( int[] signature, int band_number, int band_size ) {

        int offset = band_number * band_size;

        the_hash = bigprime;

        for( int index = offset; index < offset + band_size; index++ ) {

                the_hash = the_hash * signature[index];
        }
    }

    public int hashCode() {
        return the_hash;
    }

    public String toString() {
        return Integer.toString(the_hash);
    }

    public boolean equals( Object o ) {
        return o instanceof Band && (o.hashCode() == this.hashCode());
    }
}
