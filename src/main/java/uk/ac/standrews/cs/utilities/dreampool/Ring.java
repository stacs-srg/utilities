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
package uk.ac.standrews.cs.utilities.dreampool;

import java.util.BitSet;

/**
 * Maintains a ring of data (all inclusive points at some distance bounds from the centroid).
 *
 * Created by al on 26/2/2018.
 */
public class Ring<T> {

    public final BitSet contents;
    public final double radius;

    public Ring( double radius ) {
        contents = new BitSet();
        this.radius = radius;
    }

    public void add( int index ) {
        contents.set(index);
    }

    public int size() {
        return contents.cardinality();
    }
}
