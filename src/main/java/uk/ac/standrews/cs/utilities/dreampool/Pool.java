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

import uk.ac.standrews.cs.utilities.m_tree.Distance;

/**
 * A pool is a pivot (centroid) plus a collection of rings
 * This therefore maps from a pivot to a set of rings containing data points
 */

public class Pool<T> {

    final T pivot;

    public static final float[] DEFAULT_RADII = new float[]{0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.45F }; //, 0.5F};    // TODO make more efficient later

    public final float[] radii;
    private final Distance<T> distance_wrapper;
    private Ring<T>[] rings;
    private int last_index;
    private float max_radius;

    public Pool(T pivot, Distance<T> distance) {
        this(pivot,DEFAULT_RADII, distance);
    }

    public Pool(T pivot, float[] radii, Distance<T> distance_wrapper) {

        this.pivot = pivot;
        this.radii = radii;
        this.distance_wrapper = distance_wrapper;
        initialise_rings();
    }


    private void initialise_rings() {
        Ring r = null;
        this.rings= new Ring[radii.length];
        last_index = rings.length - 1;

        for (int i = 0; i <= last_index; i++ ) {
            r = new Ring<>(this, i, calc_ring_min( i ), radii[i], r);
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

    public void add(T datum) throws Exception {

        float distance = distance_wrapper.distance(pivot,datum);
        if( distance < maxR() ) {
            Ring<T> ring = findEnclosingRing(distance);
            ring.add(datum);
        }
    }

    public Ring<T> findEnclosingRing(float distance) throws Exception {

        if (distance > max_radius) {
            return null;
        }
        for (int index = 0; index <= last_index; index++) { // TODO make more efficient later
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
        for (int index = 0; index <= last_index; index++) { // TODO make for efficent later
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

    public T getPivot() {
        return pivot;
    }

    public float maxR() {
        return max_radius;
    }

    public float[] getRadii() { // TODO Remove debug method only
        return radii;
    }

    public Ring<T>[] getRings() {
        return rings;
    }

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

}
