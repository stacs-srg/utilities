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
package uk.ac.standrews.cs.utilities.dreampool;

import uk.ac.standrews.cs.utilities.m_tree.DataDistance;
import uk.ac.standrews.cs.utilities.m_tree.Distance;

import java.util.*;

public class MPool<T> { // aka DreamPool

    private final Set<T> pivots;
    private final Distance<T> distance_wrapper;

    List<Pool<T>> pools = new ArrayList<>();
    private Ring<T> universal_ring = new Ring<T>(null,0,0.0f, 1.0F, null ); // a ring containing all the objects in the universe

    /**
     * @param pivots
     * @param radii
     *
     */
    public MPool(Distance<T> distance_wrapper, Set<T> pivots, float[] radii) throws Exception {

        this.pivots = pivots;
        this.distance_wrapper = distance_wrapper;

        initialise(radii,distance_wrapper);
    }

    public MPool(Distance<T>  distance, Set<T> ros) throws Exception {
        this( distance, ros, Pool.DEFAULT_RADII );
    }


    private void initialise(float[] radii, Distance<T> distance_wrapper) throws Exception {

        for( T pivot : pivots ) {
            if( radii == null ) {
                pools.add(new Pool(pivot,distance_wrapper));
            } else {
                pools.add(new Pool(pivot,radii,distance_wrapper));
            }
        }
    }


    public void add(T datum) throws Exception {

        for( Pool<T> pool : pools ) {
            pool.add(datum);
        }
        universal_ring.add(datum);
    }

    /**
     * Find the nodes within range r of query.
     *
     * @param query - some data for which to find the neighbours within distance r
     *
     * @return all those nodes within r of @param T.
     *
     * General technique - find the rings that overlap with the query then do exclusion
     * using pivot exclusion.
     *
     * Then exclude the rings that do not.
     *
     */
    public Set<T> rangeSearch(final T query, final float threshold, Query<T> query_obj) { // , Query<T> query_obj) { // TODO query_obj only for validation

        List<Ring<T>> include_list = new ArrayList<>(); // circles that cover query and may contain soln
        List<Ring<T>> exclude_list = new ArrayList<>(); // circles that do not cover query and may are not part of soln

        for (Pool<T> pool : pools) {
            float distance_query_pivot = distance_wrapper.distance(pool.getPivot(),query);

            // Uses: pivot exclusion (b) For a reference point p ∈ U and any real value μ,
            // if d(q,p) ≤ μ−t, then no element of {s ∈ S | d(s,p) > μ} can be a solution to the query

            Ring<T> r1 = pool.findIncludeRing(distance_query_pivot, threshold);

            if (r1 != null) {
                // any circles that are added to include_list cover query.
                include_list.add(r1);
            }
            // uses pivot exclusion (a) For a reference point p ∈ U and any real value μ,
            // if d(q,p) > μ+t, then no element of {s ∈ S | d(s,p) ≤ μ} can be a solution to the query
            Ring r2 = pool.findExcludeRing(distance_query_pivot, threshold);
            if (r2 != null) {
                exclude_list.add(r2); // to be refined below.
            }

        }

        include_list.add( universal_ring );
        query_obj.validateIncludeList(include_list, query_obj);
        Set<T> candidates = intersections(include_list,query_obj);
        query_obj.validateOmissions(candidates,include_list);
        int excluded = exclude( candidates, exclude_list );
        int count = filter( candidates, query, threshold );
        return candidates;
    }

    private List<Ring<T>> extractTsFromDDs(List<DataDistance<Ring<T>>> include_list) {
        List<Ring<T>> result = new ArrayList<>();
        for( DataDistance<Ring<T>> dd : include_list ) {
            result.add(dd.value);
        }
        return result;
    }


    /*********************************** private methods ***********************************/


    /**
     * Attemts to find an inner ring that does not overlap with the query
     * @param ring
     * @param distance_query_pivot
     * @param threshold
     * @param query_obj
     * @return an inner ring that does not overlap with the query, if there is one and null overwise
     */
    private Ring<T> uncover(Ring<T> ring, float distance_query_pivot , float threshold, Query<T> query_obj) {

        while( ring != null && distance_query_pivot - ring.getRmax() < threshold ) { // there is overlap
            ring = ring.getInnerRing();
        }
        return ring;
    }


    public void show_structure() {
        for( Pool pool : pools ) {
            pool.show_structure();
        }
    }

    public void showRings( List<Ring<T>> list) {
        int i = 1;
        for( Ring<T> ring : list ) {
            System.out.println( i++ + ": " + ring.getOwner().getPivot() + " ring: " + ring.getRing_number() + " size: " + ring.size() );
        }
        System.out.println( "-------");
    }

    private Set<T> intersections(List<Ring<T>> include_list, Query<T> query_obj) { // }, Query<T> query_obj) {  // TODO query_obj only for debug
        if( include_list.isEmpty() ) {
            return new HashSet<T>();
        } else {
            int index = findSmallestSetIndex( include_list );
            Set<T> result = new HashSet<T>( include_list.get(index).getAllContents() );
            include_list.remove(index);

            if( include_list.size() == 0 ) {
                // there was only one ring that enclosed it so that is the result
                return result;
            } else {
                // otherwise do the intersection of all the enclosing rings.
                for (Ring<T> remaining_rings : include_list) {
                    result = intersection(result, remaining_rings.getAllContents(),query_obj); // ,query_obj );
                }
            }
            return result;
        }
    }

    private int exclude(Set<T> candidates, List<Ring<T>> exclude_list) {
        int count = 0;
        for( Ring<T> ring : exclude_list ) {
            ArrayList<T> ring_contents = ring.getAllContents();
            if( candidates != null && ! candidates.isEmpty() ) {
                for (T candidate : ring_contents) {
                    if (candidates.remove(candidate)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int filter(Set<T> candidates, T query, float threshold) {
        int false_positives = 0;
        int true_positives = 0;
        int distance_calcs = 0;

        Set<T> dropset = new HashSet<>();

        if( candidates != null && ! candidates.isEmpty() ) {
            for (T candidate : candidates) {
                distance_calcs++;

                if (distance_wrapper.distance(query, candidate) > threshold) {
                    dropset.add(candidate);     // How do you write this without ConcurrentModificationException ??
                    false_positives++;
                } else {
                    true_positives++;
                }
            }
        }
        for( T member : dropset ) {
            candidates.remove(member);
        }

//        System.out.println( "True positives (before filtering): " + true_positives );
//        System.out.println( "False positives (before filtering): " + false_positives );
//        System.out.println( "Distance calculations (during filtering): " + distance_calcs );
        return true_positives;
    }

    int intersection_count = 1;

    public Set<T> intersection(Collection<T> a, Collection<T> b, Query<T> query_obj) { // }, Query<T> query_obj) { // TODO query_obj only for debug
        Set<T> result = new HashSet<>();
        for( T next : a ) {
            if( b.contains( next ) ) {
                result.add( next );  // point in a and b => add to intersection.
            } else {                 // excluding an item
                // TODO DEBUG
                float d = distance_wrapper.distance(query_obj.query, next);
                if (d <= query_obj.threshold) {
                    System.err.println(intersection_count + " # Wrongly excluding item: " + next + " within threshold " + query_obj.threshold + " d = " + d);
                }
//                if( query_obj.real_solutions.contains(next)) {
//                    System.err.println( "Eliminating point that should be part of soln space: " + next );
//                }
            }
        }
        intersection_count++;
        return result;
    }

    public static Set union(Collection a, Collection b) {
        Set result = new HashSet();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    ////// Private

    /**
     * PRE - include_list is not empty.
     * @param include_list
     * @return
     */
    private int findSmallestSetIndex(List<Ring<T>> include_list) {
        int smallest = Integer.MAX_VALUE;
        int result = -1;
        for( int i = 0; i < include_list.size(); i++ ) {
            int size = include_list.get(i).size();
            if( size < smallest ) {
                smallest = size;
                result = i;
            }
        }
        return result;
    }
}
