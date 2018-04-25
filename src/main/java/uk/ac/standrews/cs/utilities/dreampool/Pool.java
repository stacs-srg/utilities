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
 * A pool is a pivot (centroid) plus a collection of rings
 * This therefore maps from a pivot to a set of rings containing data points
 */

public class Pool<T> {

    public final T pivot;                       // the pivot (the centre) of the pool.
    public final int pool_id;                   // the index of this pool into (any) array of distances etc. indexed by
    public final int num_pools;                 // number of pools in the system - copied down from MPool - what would Martin Fowler say? [good/bad]- discuss not sure - al.
    public final Ring<T>[] rings;               // an array of rings, each of which holds the elements drawn from s that are within the ring (rings are inclusive => include the elements in inner rings)
    public final BitSet[] closer_than;          // Used to store information for hyperplane exclusion - is this closer_then[i] says this pivot is closer than pivot i. // TODO This probably should not be here.

    public Pool(T pivot, int pool_id, int num_pools, double[] radii) {

        this.pivot = pivot;
        this.pool_id = pool_id;
        this.num_pools = num_pools;
        this.rings= new Ring[radii.length];

        for (int i = 0; i <= rings.length - 1; i++ ) {
            rings[i] = new Ring<T>(radii[i]);
        }

        closer_than = new BitSet[num_pools];
        for( int i = 0; i < closer_than.length; i++ ) {
            closer_than[i] = new BitSet();
        }
    }


    public void add(int element_id, double[] distances_from_datum_to_pivots) {

        double distance_from_datum_to_pivot = distances_from_datum_to_pivots[pool_id];

        // add this element to each ring if it is within the ball of ring.

        for (Ring r : rings) {
            if (distance_from_datum_to_pivot <= r.radius) {
                r.add(element_id);
            }
        }

        /** Next add this element to the hyperplane exclusion data structure
         *  Uses hyperplane exclusion
         **/

        for( int i = 0; i < num_pools; i++ ) {
            if( distance_from_datum_to_pivot < distances_from_datum_to_pivots[i] ) { // is this pivot closer than the other?
                    closer_than[i].set(element_id);
            }
        }
    }

}












