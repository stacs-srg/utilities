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

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.IntSet;
import uk.ac.standrews.cs.utilities.m_tree.Distance;

import java.util.*;

/**
 * @author al@st-andrews.ac.uk
 * @param <T> The type of the elements stored in this data structure.
 *
 */
public class MPool<T> { // aka DreamPool

    private final Set<T> pivots;                            // the set of all pivots in the system.
    private int num_pools;                                  // number of pools (=number of pivots) in the implementation
    private final Distance<T> distance_wrapper;             // the distance function used in the implementation.

    List<Pool<T>> pools = new ArrayList<>();                // the set of pools containing rings and pivots.
    private int element_id = 0;                             // an int used to represent each of the elements used in s, used to index values TreeMap.
    private TreeMap<Integer,T> values = new TreeMap<>();    // used to store mappings from indices to the elements of s (to facilitate lookup from bitmaps to datums).

    private Ring<T> universal_ring =
            new Ring<T>(null,
                        this,
                        0,
                        0.0f,
                        1.0F,
                        null );                    // a ring containing all the objects in the universe (this is a catch all to ensure coverage of all points)


    /**
     * @param pivots
     * @param radii
     *
     */
    public MPool(Distance<T> distance_wrapper, Set<T> pivots, float[] radii) throws Exception {

        this.pivots = pivots;
        this.num_pools = pivots.size();
        this.distance_wrapper = distance_wrapper;

        initialise(radii,distance_wrapper);
    }

    public MPool(Distance<T>  distance, Set<T> ros) throws Exception {
        this( distance, ros, Pool.DEFAULT_RADII );
    }


    private void initialise(float[] radii, Distance<T> distance_wrapper) throws Exception {

        int pool_id = 0;

        for( T pivot : pivots ) {
            if( radii == null ) {
                pools.add(new Pool(pivot, pool_id, num_pools, this, distance_wrapper)); // default radii
            } else {
                pools.add(new Pool(pivot, pool_id, num_pools, radii, this, distance_wrapper));
            }
            pool_id++;
        }
    }


    public void add(T datum) throws Exception {

        float[] distances_from_datum_to_pivots = new float[num_pools];
        int distance_index = 0;

        values.put( element_id,datum );         // add to the master index to facilitate lookup from bitmaps to datums.
        for( Pool<T> pool : pools ) {           // calculate the distances to all the pivots.
            distances_from_datum_to_pivots[ distance_index++ ] = distance_wrapper.distance(pool.getPivot(),datum);
        }
        for( Pool<T> pool : pools ) {
            pool.add(element_id, datum, distances_from_datum_to_pivots );
        }

        universal_ring.add(element_id);
        element_id++;
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
     */
    public Set<T> rangeSearch(final T query, final float threshold, Query<T> query_obj ) { // NOTE query_obj only for validation

        List<Ring<T>> include_list = new ArrayList<>(); // circles that cover query and may contain soln
        List<Ring<T>> exclude_list = new ArrayList<>(); // circles that do not cover query and may are not part of soln

        float[] distances_from_query_to_pivots = new float[num_pools];
        int distance_index = 0;

        for (Pool<T> pool : pools) {
            float distance_query_pivot = distance_wrapper.distance(pool.getPivot(),query);
            distances_from_query_to_pivots[ distance_index++ ] = distance_query_pivot;              // save this for HP exclusion - used below.

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

        /** Next perform hyperplane exclusion: For a reference point pi ∈ U,
         ** If d(q,p1) - d(q,p2) > 2t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
         ** Here we are performing the first part of this - d(s,p1) ≤ d(s,p2), first second part evaluated at initialisation time.
         **/

        ConciseSet all_hp_exlusions = new ConciseSet();

        for (Pool<T> pool : pools) {
            ConciseSet next_hp_exlude_set = pool.findHPExclusion(distances_from_query_to_pivots, threshold);
            query_obj.validateHPExclusions(getValues(next_hp_exlude_set));
            all_hp_exlusions.addAll(next_hp_exlude_set);
        }

        System.out.println( " all_hp_exlusions size = " + all_hp_exlusions.size() );

        include_list.add( universal_ring );
        query_obj.validateIncludeList(include_list, query_obj);
        ConciseSet candidates = intersections(include_list,query_obj);

        System.out.println( " Intersection size = " + candidates.size() );

        query_obj.validateOmissions(getValues(candidates),include_list);
        candidates = exclude( candidates, exclude_list );

        System.out.println( " candidates size (after pivot exclusion) before hp exclusion = " + candidates.size() );

        candidates = candidates.difference(all_hp_exlusions);

        System.out.println( " candidates size after hp exclusion = " + candidates.size() );

        int count = filter( candidates, query, threshold );
        return getValues( candidates );
    }

    public T getValue( int index ) {
        return values.get( index );
    }

    public Set<T> getValues(ConciseSet candidates) {
        Set<T> result = new HashSet<>();
        IntSet.IntIterator iter = candidates.iterator();

        while( iter.hasNext() ) {

            result.add( getValue( iter.next() ) );

        }
        return result;
    }

    public void showRings( List<Ring<T>> list) {
        int i = 1;
        for( Ring<T> ring : list ) {
            System.out.println( i++ + ": " + ring.getOwner().getPivot() + " ring: " + ring.getRing_number() + " size: " + ring.size() );
        }
        System.out.println( "-------");
    }

    public void show_structure() {
        for( Pool pool : pools ) {
            pool.show_structure();
        }
    }


    private ConciseSet intersections(List<Ring<T>> include_list, Query<T> query_obj) { // }, Query<T> query_obj) {  // TODO query_obj only for debug
        if( include_list.isEmpty() ) {
            return new ConciseSet();
        } else {
            int index = findSmallestSetIndex( include_list );
            ConciseSet result = include_list.get(index).getConciseContents().clone();
            include_list.remove(index);

            if( include_list.size() == 0 ) {
                // there was only one ring that enclosed it so that is the result
                return result;
            } else {
                // otherwise do the intersection of all the enclosing rings.
                for (Ring<T> remaining_rings : include_list) {
                    result = result.intersection( remaining_rings.getConciseContents() );
                }
            }
            return result;
        }
    }

    private ConciseSet exclude(ConciseSet candidates, List<Ring<T>> exclude_list) {
        ConciseSet result = new ConciseSet();

        for( Ring<T> ring : exclude_list ) {
            ConciseSet ring_contents = ring.getConciseContents();

            result = candidates.difference(ring_contents);

        }
        return result;
    }

    private int filter(ConciseSet candidates, T query, float threshold) {
        int false_positives = 0;
        int true_positives = 0;

        Set<Integer> dropset = new HashSet<>();

        if (candidates != null && !candidates.isEmpty()) {

            IntSet.IntIterator iter = candidates.iterator();

            while (iter.hasNext()) {

                int next = iter.next();
                T candidate = values.get(next);
                if (candidate != null) {
                    if (distance_wrapper.distance(query, candidate) > threshold) {
                        dropset.add(next);
                        false_positives++;
                    } else {
                        true_positives++;
                    }
                }
            }
            for (int member : dropset) {
                candidates.flip(member);
            }

        }
        return true_positives;
    }

    int intersection_count = 1;

    public Set<T> intersection(Collection<T> a, Collection<T> b, Query<T> query_obj) { // }, Query<T> query_obj) { // TODO query_obj only for debug
        Set<T> result = new HashSet<>();
        for( T next : a ) {
            if( b.contains( next ) ) {
                result.add( next );  // point in a and b => add to intersection.
            } else {                 // excluding an item
                // next 4 lines are DEBUG
//                float d = distance_wrapper.distance(query_obj.query, next);
//                if (d <= query_obj.threshold) {
//                    System.err.println(intersection_count + " # Wrongly excluding item: " + next + " within threshold " + query_obj.threshold + " d = " + d);
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

    public void completeInitialisation() throws Exception {
        for( Pool<T> pool : pools ) {
            pool.completeInitialisation();
        }
    }

    /*********************************** private methods ***********************************/


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
