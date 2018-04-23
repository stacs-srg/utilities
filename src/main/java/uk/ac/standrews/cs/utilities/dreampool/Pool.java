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

import org.roaringbitmap.RoaringBitmap;
import uk.ac.standrews.cs.utilities.m_tree.Distance;

/**
 * A pool is a pivot (centroid) plus a collection of rings
 * This therefore maps from a pivot to a set of rings containing data points
 */

public class Pool<T> {

    public static final float[] DEFAULT_RADII = new float[]{0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.45F };    // TODO make more efficient later - Richards suggestion use median distances.

    final T pivot;                              // the pivot (the centre) of the pool.
    public float[] radii;                       // the array of distances being used to define the size of the rings in this pool
    private final Distance<T> distance_wrapper; // the distance function being used in this implementation
    private Ring<T>[] rings;                    // an array of rings, each of which holds the elements drawn from s that are within the ring (rings are inclusive => include the elements in inner rings)
    private int last_index;                     // last index into the array of rings
    private float max_radius;                   // the maximum radius of this pool
    private int pool_id;                        // the index of this pool into (any) array of distances etc. indexed by
    private int num_pools;                      // number of pools in the system - copied down from MPool - what would Martin Fowler say? [good/bad]- discuss not sure - al.
    public RoaringBitmap[] closer_than;         // Used to store information for hyperplane exclusion - see comments in add below.
    private final MPool<T> owner;               // The MPool to which this pool belongs

    public Pool(T pivot, int pool_id, int num_pools,  MPool<T> owner, Distance<T> distance) {
        this(pivot, pool_id, num_pools, DEFAULT_RADII, owner, distance);
    }

    public Pool(T pivot, int pool_id, int num_pools, float[] radii, MPool<T> owner, Distance<T> distance_wrapper) {

        this.pivot = pivot;
        this.pool_id = pool_id;
        this.num_pools = num_pools;
        this.radii = radii;
        this.owner = owner;
        this.distance_wrapper = distance_wrapper;
        initialise_rings();

        closer_than = new RoaringBitmap[num_pools];
        for( int i = 0; i < closer_than.length; i++ ) {
            closer_than[i] = new RoaringBitmap();
        }
    }


    private void initialise_rings() {
        Ring r = null;
        this.rings= new Ring[radii.length];
        last_index = rings.length - 1;

        for (int i = 0; i <= last_index; i++ ) {
            r = new Ring<>(this, owner, i, calc_ring_min( i ), radii[i], r);
            rings[i] = r;
        }
        max_radius = r.getRmax();
    }

    private float calc_ring_min(int index) {
        if( index == 0 ) {
            return 0;
        }
        return radii[index-1];
    }

    public void add(int element_id, T datum, float[] distances_from_datum_to_pivots) throws Exception {

        float distance_from_datum_to_pivot = distances_from_datum_to_pivots[pool_id];

        // First add this element to the appropriate ring if it datum is within the ball of this pool.

        if( distance_from_datum_to_pivot < maxR() ) {
            Ring<T> ring = findEnclosingRing(distance_from_datum_to_pivot);
            ring.add(element_id);
        }

        /** Next add this element to the hyperplane exclusion data structure
         *  Uses hyperplane exclusion
         **/

        for( int i = 0; i < num_pools; i++ ) {
            if( i != pool_id && distance_from_datum_to_pivot <= distances_from_datum_to_pivots[i] ) { // is this pivot closer than the other?
                    closer_than[i].add(element_id);
            }
        }
    }

    public Ring<T> findEnclosingRing(float distance) throws Exception {

        if (distance > max_radius) {
            return null;
        }
        for (int index = 0; index <= last_index; index++) {
            if (distance < radii[index]) {
                return rings[index];
            }
        }
        throw new Exception( "findEnclosingRing - code should never be reached" );
    }

    /**
     * Uses pivot exclusion (b) For a reference point p ∈ U and any real value μ,
     * if d(q,p) ≤ μ−t, then no element of {s ∈ S | d(s,p) > μ} can be a solution to the query
     *
     * @param distance_query_pivot - the distance from the query point to this pivot
     * @param threshold            - the threshold around the query.
     * @return an enclosing ring that overlaps with query at threshold, or null if there is no overlap
     */
    public Ring<T> findIncludeRing(float distance_query_pivot, float threshold) {

        if (distance_query_pivot <= max_radius - threshold) {
            // if we get past here the outer ring overlaps the query,
            // that is outer ring covers query
            // trying to find the smallest one that covers the query

            Ring<T> result = rings[last_index]; // we know this is a good result from above

            // search from outside - find the smallest
            for (int valid_index = last_index; valid_index >= 0; valid_index--) {
                if (distance_query_pivot <= rings[valid_index].getRmax() - threshold) {
                    result = rings[valid_index];
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Uses pivot exclusion (a) For a reference point p ∈ U and any real value μ,
     * if d(q,p) > μ+t, then no element of {s ∈ S | d(s,p) ≤ μ} can be a solution to the query
     *
     * @param distance_query_pivot - the distance from the query point to this pivot
     * @param threshold - the threshold around the query.
     * @return an enclosing ring that does not overlaps with query at threshold, or null if there is overlap by all rings
     */
    public Ring findExcludeRing(float distance_query_pivot, float threshold) {

        // overlap condition distance_query_pivot < radii[index] + threshold = TRUE

        int candidate = -1;

        // search from inside - find the biggest non overlapping
        for (int index = 0; index <= last_index; index++) {
            if (distance_query_pivot >= radii[index] + threshold) {                // no overlap with query
                // this radius is a solution - may be bigger rings.
                candidate = index;
            } else {
                // we have overlap so stop
                break;
            }
        }
        if( candidate == -1 ) {
            return null;
        } else {
            return rings[candidate];
        }

    }

    public void setRadii(float[] radii) {
        this.radii = radii;
    }

    public T getPivot() {
        return pivot;
    }

    public float maxR() {
        return max_radius;
    }

    public MPool<T> getOwner() {
        return owner;
    }

    public int getPoolId() { return pool_id; }

    //--------------------


    public void show_structure() {
        int i = 0;
        System.out.println("Pool with pivot: " + pivot);
        for (float radius : radii) {
            Ring<T> ring = rings[i++];
            System.out.println("\tRing r_min: " + ring.getRmin() + "r_max: " + ring.getRmax());
//            ArrayList<DataDistance<T>> list = ring.getAllDistances();
//            for (DataDistance<T> dd : list) {
//                System.out.println("\tNode: " + dd.value + " distance: " + dd.distance);
//            }
        }
    }

    public void completeInitialisation() throws Exception {
        for( Ring<T> ring : rings ) {
            ring.consolidateSets();
        }
    }

    /** Uses 3 point hyperplane exclusion: For a reference point pi ∈ U,
     ** If d(q,p1) - d(q,p2) > 2t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
     ** Here we are initialising the second part of this - d(s,p1) ≤ d(s,p2), first part evaluated at query time.
     **/
    public RoaringBitmap findHPExclusion3P(RoaringBitmap exclusions, float[] distances_from_query_to_pivots, float threshold) {

        float distance_from_query_to_this_pivot = distances_from_query_to_pivots[pool_id];

        for( int i = 0; i < num_pools; i++ ) {

            if (i != pool_id && distance_from_query_to_this_pivot - distances_from_query_to_pivots[i] > 2 * threshold) {

                exclusions.or(closer_than[i]); // was addAll

            }
        }
        return exclusions;
    }


    private float square( float a ) { return a * a; }


}












