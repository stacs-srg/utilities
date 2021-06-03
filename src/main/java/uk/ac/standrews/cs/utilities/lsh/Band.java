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

/**
 * Created by al on 07/09/2017 at the University of Liverpool.
 */
public class Band {

    private static final int bigprime = 2147483647;
    private static final int someprime = 16777619;
    private int the_hash;

    Band(Integer[] signature, int band_number, int band_size) {

        int offset = band_number * band_size;

        the_hash = salt_hash(bigprime, band_number);  // seed the hash function with the band - every band hashes differently.

        for (int index = offset; index < offset + band_size; index++) {
            the_hash = salt_hash(the_hash, signature[index]); // use all the integers in the nad to calculate the hash code for this band,
        }
    }

    private static int salt_hash(int salt, int value) {
        return (salt * someprime) ^ value;
    }

    @Override
    public int hashCode() {
        return the_hash;
    }

    public String toString() {
        return Integer.toString(the_hash);
    }

    public boolean equals(Object o) {
        return o instanceof Band && (o.hashCode() == this.hashCode());
    }
}
