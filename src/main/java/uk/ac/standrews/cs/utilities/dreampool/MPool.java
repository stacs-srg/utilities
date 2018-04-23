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
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.m_tree.Distance;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 *
 * A Metric datastructure based on pivot exclusion and hyperplane exclusion.
 * @author al@st-andrews.ac.uk
 * @param <T> The type of the elements stored in this data structure.
 *
 */
public class MPool<T> {

    private final List<T> pivots;                            // the set of all pivots in the system.
    private int num_pools;                                  // number of pools (=number of pivots) in the implementation
    private final Distance<T> distance_wrapper;             // the distance function used in the implementation.

    List<Pool<T>> pools = new ArrayList<>();                // the set of pools containing rings and pivots.
    private int element_id = 0;                             // an int used to represent each of the elements used in s, used to index values TreeMap.
    private List<T> values = new ArrayList<>();             // used to store mappings from indices to the elements of s (to facilitate lookup from bitmaps to datums).

    private Ring<T> universal_ring =
            new Ring<T>(null,
                        this,
                        0,
                        0.0f,
                        1.0F,
                        null );                    // a ring containing all the objects in the universe (this is a catch all to ensure coverage of all points)
    private float[][] inter_pivot_distances;


    /**
     * Create MPool using specified radii
     * @param distance_wrapper - a distance function that determines distance between two Ts
     * @param pivots - a set of pivots/reference objects to use.
     * @param radii - a set of radii to form pools (balls) around the pivots.
     */
    public MPool(Distance<T> distance_wrapper, List<T> pivots, float[] radii) throws Exception {
        this.pivots = pivots;
        this.distance_wrapper = distance_wrapper;
        this.num_pools = pivots.size();

        initialise_pivot_distances( pivots );

        System.out.println( "Radii:");
        for( int i = 0;i < radii.length; i++ ) {
            System.out.println( "\t" + radii[i] );
        }
        initialisePools(radii,distance_wrapper);
    }

    /**
     * Create MPool using default radii
     * @param distance
     * @param ros
     * @throws Exception
     */
    public MPool(Distance<T>  distance, List<T> ros) throws Exception {
        this( distance, ros, Pool.DEFAULT_RADII );
    }


    public void add(T datum) throws Exception {

        float[] distances_from_datum_to_pivots = new float[num_pools];
        int pivot_index = 0;

        values.add( datum );                    // add to the master index to facilitate lookup from bitmaps to datums.
        for( Pool<T> pool : pools ) {           // calculate the distances to all the pivots.
            distances_from_datum_to_pivots[ pivot_index++ ] = distance_wrapper.distance(pool.getPivot(),datum);
        }
        for( Pool<T> pool : pools ) {
            pool.add(element_id, datum, distances_from_datum_to_pivots );
        }

        universal_ring.add(element_id);
        element_id++;
    }

    private void initialisePools(float[] radii, Distance<T> distance_wrapper) throws Exception {

        int pool_id = 0;

        for( T pivot : pivots ) {
            pools.add(new Pool(pivot, pool_id, num_pools, radii, this, distance_wrapper));
            pool_id++;
        }

    }

    /**
     * @param pivots - the set of pivots being used in this instance
     *
     * initialises an 2D array of inter pivot distances called inter_pivot_distances
     */
    private void initialise_pivot_distances(List<T> pivots) {
        inter_pivot_distances = new float[num_pools][num_pools];
        int i = 0;
        for( T p1 : pivots ) {
            int j = 0;
            for( T p2 : pivots ) {
                float d = distance_wrapper.distance(p1,p2);
                inter_pivot_distances[ i ][ j ] = d;
                inter_pivot_distances[ j ][ i ] = d;
                j++;
            }
            i++;
        }
    }

    /**
     * Find the nodes within range r of query.
     *
     * @param query - some data for which to find the neighbours within the distance specified by threshold
     * @param threshold - the threshold distance specifying the size of the query ball
     * @param query_obj - the query being performed (for diagnostics) - permits extra data to be passed in and out and validation
     *
     * @return all those nodes from S within @param threshold
     *
     * General technique - find the rings that overlap with the query then do inclusion,
     * find the rings that do not overlap using and do exclusion,
     * finally perform hyperplane exclusion,
     * filter the results to exclude false positives.
     */
    public Set<T> rangeSearch(final T query, final float threshold, Query<T> query_obj) { // NOTE query_obj only for validation

        List<Ring<T>> include_list = new ArrayList<>(); // circles that cover query and may contain soln
        List<Ring<T>> exclude_list = new ArrayList<>(); // circles that do not cover query and may are not part of soln

        float[] distances_from_query_to_pivots = new float[num_pools];
        int distance_index = 0;

        for (Pool<T> pool : pools) {
            float distance_query_pivot = distance_wrapper.distance(pool.getPivot(), query);
            distances_from_query_to_pivots[distance_index++] = distance_query_pivot;              // save this for HP exclusion - used below.
        }

        RoaringBitmap exclusions = new RoaringBitmap();

        for (Pool<T> pool : pools) {

            float distance_query_pivot = distances_from_query_to_pivots[pool.getPoolId()];

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

            /** Next perform hyperplane exclusion: For a reference point pi ∈ U,
             ** If d(q,p1) - d(q,p2) > 2t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
             ** Here we are performing the first part of this - d(s,p1) ≤ d(s,p2), first second part evaluated at initialisation time.
             **/

            exclusions = findHPExclusion4P(exclusions, distances_from_query_to_pivots, threshold);
            query_obj.validateHPExclusions(exclusions);
        }

        int hp_exclusions = exclusions.getCardinality();
        query_obj.setHPexclusions( hp_exclusions );

        include_list.add( universal_ring );
        query_obj.validateIncludeList(include_list, query_obj);
        RoaringBitmap inclusions = intersections(include_list,query_obj);

        int pivot_inclusions = inclusions.getCardinality();
        query_obj.setPivotInclusions( pivot_inclusions );

        query_obj.validateOmissions(inclusions);

        exclusions = exclude( exclusions, exclude_list );

        int pivot_exclusions = exclusions.getCardinality();
        query_obj.setPivotExclusions( pivot_exclusions );

        inclusions.andNot(exclusions); // was inclusions = inclusions.difference

        query_obj.setRequiringFiltering( inclusions.getCardinality() );

        int count = filter( inclusions, query, threshold );

        return getValues( inclusions );
    }

    /**
     * Find the nodes within range r of query.
     *
     * @param query - some data for which to find the neighbours within the distance specified by threshold
     * @param threshold - the threshold distance specifying the size of the query ball
     * @param query_obj - the query being performed (for diagnostics) - permits extra data to be passed in and out and validation
     *
     * @return all those nodes from S within @param threshold
     *
     * General technique - find the rings that overlap with the query then do inclusion,
     * find the rings that do not overlap using and do exclusion,
     * finally perform hyperplane exclusion,
     * filter the results to exclude false positives.
     */
    public Set<T> parallelRangeSearch(final T query, final float threshold, ExecutorService executor, Query<T> query_obj) { // NOTE query_obj only for validation

        RoaringBitmap[] inclusions_vector = new RoaringBitmap[ num_pools ];
        RoaringBitmap[] exclusions_vector = new RoaringBitmap[ num_pools ];
        RoaringBitmap[] hp_exclusions_vector = new RoaringBitmap[ num_pools ];

        float[] distances_from_query_to_pivots = new float[num_pools];

        final int batch_size = 4;

        final CountDownLatch latch1 = new CountDownLatch(num_pools) ;
        // calculate distance_query_pivot in parallel
        for ( int start = 0; start <= num_pools; start += batch_size) {
            final int start_f = start;
            executor.submit(() -> {
                for (int index = start_f; index < start_f + batch_size; index++) {
                    float distance_query_pivot = distance_wrapper.distance(pools.get(index).getPivot(), query);
                    distances_from_query_to_pivots[index] = distance_query_pivot;              // save this for HP exclusion - used below

                    Pool<T> pool = pools.get(index);

                    // Uses: pivot exclusion (b) For a reference point p ∈ U and any real value μ,
                    // if d(q,p) ≤ μ−t, then no element of {s ∈ S | d(s,p) > μ} can be a solution to the query

                    Ring<T> r1 = pool.findIncludeRing(distance_query_pivot, threshold);

                    if (r1 != null) {
                        // any circles that are added to include_list cover query.
                        inclusions_vector[index] = r1.getConciseContents();
                    }
                    // uses pivot exclusion (a) For a reference point p ∈ U and any real value μ,
                    // if d(q,p) > μ+t, then no element of {s ∈ S | d(s,p) ≤ μ} can be a solution to the query
                    Ring r2 = pool.findExcludeRing(distance_query_pivot, threshold);
                    if (r2 != null) {
                        exclusions_vector[index] = r2.getConciseContents();
                    }
                    latch1.countDown();
                }

            });
        }
        barrier( latch1 );
        // Need to block here until distances_from_query_to_pivots have been calculated.

        final CountDownLatch latch2 = new CountDownLatch(num_pools) ;
        // calculate HP exclusions in parallel
        for ( int start = 0; start <= num_pools; start += batch_size) {
            final int start_f = start;
            /** Next perform hyperplane exclusion: For a reference point pi ∈ U,
             ** If d(q,p1) - d(q,p2) > 2t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
             ** Here we are performing the first part of this - d(s,p1) ≤ d(s,p2), first second part evaluated at initialisation time.
             **/
            executor.submit(() -> {
                for ( int index = start_f; index < start_f + batch_size; index++) {
                    Pool<T> pool = pools.get(index);
                    hp_exclusions_vector[index] = findParallelHPExclusion4P(distances_from_query_to_pivots, threshold);
                    latch2.countDown();
                }

            });
        }
        barrier( latch2 );

        RoaringBitmap hp_exclusions = new RoaringBitmap();
        RoaringBitmap pivot_inclusions  = universal_ring.getConciseContents().clone();
        RoaringBitmap pivot_exclusions = new RoaringBitmap();

        // At this point we have all the inclusions and exclusion bitmaps and need to reduce them.

        final CountDownLatch latch3 = new CountDownLatch(3) ; // 3 reduces below
        executor.submit(() -> {
                    combineOR( hp_exclusions, hp_exclusions_vector );
                    query_obj.setHPexclusions( hp_exclusions.getCardinality() );
                    latch3.countDown();
                } );

        executor.submit(() -> {
                    combineAND(pivot_inclusions, inclusions_vector);
                    query_obj.setPivotInclusions(pivot_inclusions.getCardinality());
                    //query_obj.validateIncludeList(include_list, query_obj);                       // TODO change signatures?
                    query_obj.validateOmissions(pivot_inclusions);
                    latch3.countDown();
                } );
        executor.submit(() -> {
                    combineOR(pivot_exclusions, exclusions_vector);
                    query_obj.setPivotExclusions(pivot_exclusions.getCardinality());
                    latch3.countDown();
                } );
        barrier( latch3 );

        // Now sequential - reduce the 3 bitmaps to 1 bitmap of results

        pivot_inclusions.andNot(pivot_exclusions);
        pivot_inclusions.andNot(hp_exclusions);

        query_obj.setRequiringFiltering( pivot_inclusions.getCardinality() );

        filter( pivot_inclusions, query, threshold );

        return getValues( pivot_inclusions );
    }

    /** Uses 4 point hyperplane exclusion: For a reference point pi ∈ U,
     ** If ( d(q,p1)2 - d(q,p2)2 ) / 2d(p1,p2) ) > t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
     ** Here we are initialising the second part of this - d(s,p1) ≤ d(s,p2), first part evaluated at query time.
     **/
    public RoaringBitmap findHPExclusion4P(RoaringBitmap exclusions, float[] distances_from_query_to_pivots, float threshold) {

        for( int i = 0; i < num_pools; i++ ) {
            float d1 = distances_from_query_to_pivots[i];
            for (int j = i + 1; j < num_pools; j++) {
                float d2 = distances_from_query_to_pivots[j];

                if ( square( d2 ) - square( d1 ) / inter_pivot_distances[i][j] > 2 * threshold ) {

                    exclusions.and(pools.get(j).closer_than[i]);

                }
//                else {
//                    if ( square( d1 ) - square( d2 ) / inter_pivot_distances[i][j] > 2 * threshold ) {
//
//                        exclusions.or(pools.get(i).closer_than[j]);
//                    }
//                }
            }

        }
        return exclusions;
    }

    /** Uses 4 point hyperplane exclusion: For a reference point pi ∈ U,
     ** If ( d(q,p1)2 - d(q,p2)2 ) / 2d(p1,p2) ) > t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
     ** Here we are initialising the second part of this - d(s,p1) ≤ d(s,p2), first part evaluated at query time.
     **/
//    public RoaringBitmap findHPExclusion4P(RoaringBitmap exclusions, float[] distances_from_query_to_pivots, float threshold) {
//
//        float distance_from_query_to_this_pivot = distances_from_query_to_pivots[pool_id];
//
//        for( int i = 0; i < num_pools; i++ ) {
//
//            if (i != pool_id && ( square(distance_from_query_to_this_pivot ) - square( distances_from_query_to_pivots[i] ) / owner.getInterPivotDistance( this.pool_id,i ) ) > 2 * threshold) {
//
//                exclusions.and(closer_than[i]);
//
//            }
//        }
//        return exclusions;
//    }


    /**
     * Thread safe version of findHPExclusion4P - no sharing
     * @param distances_from_query_to_pivots
     * @param threshold
     * @return
     */
    public RoaringBitmap findParallelHPExclusion4P(float[] distances_from_query_to_pivots, float threshold) {

        RoaringBitmap exclusions = new RoaringBitmap();

        return findHPExclusion4P( exclusions,distances_from_query_to_pivots,threshold);

    }

    private float square( float a ) { return a * a; }

    /**
     * Blocks until all threads being run by an executor have completed execution.
     * @param latch - a latch on which to wait
     */
    private void barrier(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            ErrorHandling.error("Thread interrupted");
        }
    }

    /**
     *
     * @param i - an index into the set of datums
     * @return the ith datum in the set
     */
    public T getValue( int i ) {
        return values.get( i );
    }

    /**
     *
     * @param candidates - a bitmap representing a set of values drawn from S
     * @return the set of values of type T which are represented by the bitmap
     */
    public Set<T> getValues(RoaringBitmap candidates) {
        Set<T> result = new HashSet<>();
        Iterator<Integer> iter = candidates.iterator();

        while( iter.hasNext() ) {

            result.add( getValue( iter.next() ) );

        }
        return result;
    }

    public void show_structure() {
        for( Pool pool : pools ) {
            pool.show_structure();
        }
    }

    private RoaringBitmap intersections(List<Ring<T>> include_list, Query<T> query_obj) { // }, Query<T> query_obj) {  // Note query_obj only for debug
        if( include_list.isEmpty() ) {
            return new RoaringBitmap();
        } else {
            int index = findSmallestSetIndex( include_list );
            RoaringBitmap result = include_list.get(index).getConciseContents().clone();
            include_list.remove(index);

            if( include_list.size() == 0 ) {
                // there was only one ring that enclosed it so that is the result
                return result;
            } else {
                // otherwise do the intersection of all the enclosing rings.
                for (Ring<T> remaining_rings : include_list) {
                    result.and( remaining_rings.getConciseContents() ); // was result = result.intersection
                }
            }
            return result;
        }
    }

    /**
     *
     * @param initial
     * @param vector_of_bitmaps
     * @return the intersection of the bitmaps passed in
     */
    private RoaringBitmap combineAND(RoaringBitmap initial, RoaringBitmap[] vector_of_bitmaps) {

        for( RoaringBitmap bm : vector_of_bitmaps ) {
            if( bm != null ) {
                initial.and( bm );
            }
        }
        return initial;
    }

    /**
     * @param vector_of_bitmaps
     * @return the OR of all the bitmaps in vector_of_bitmaps - this is REDUCE
     */
    private RoaringBitmap combineOR(RoaringBitmap initial, RoaringBitmap[] vector_of_bitmaps) {
       // RoaringBitmap result = new RoaringBitmap();
        for( RoaringBitmap bm : vector_of_bitmaps ) {
            if( bm != null ) {
                initial.or(bm);
            }
        }
        return initial;
    }

    private RoaringBitmap exclude(RoaringBitmap exclusions, Collection<Ring<T>> exclude_list) { //***************************************

        for( Ring<T> ring : exclude_list ) {
            RoaringBitmap ring_contents = ring.getConciseContents();

            exclusions.or(ring_contents); // was exclusions = exclusions.difference(ring_contents);

        }
        return exclusions;
    }

    private int filter(RoaringBitmap candidates, T query, float threshold) {
        int true_positives = 0;

        RoaringBitmap deletions = new RoaringBitmap();

        if (candidates != null && !candidates.isEmpty()) {    ///<<<<<<<<<<<<<< AL IS HERE

            Iterator<Integer> iter = candidates.iterator();

            while (iter.hasNext()) {

                int next = iter.next();
                T candidate = values.get(next);
                if (candidate != null) {
                    if (distance_wrapper.distance(query, candidate) > threshold) {
                        deletions.add(next);
                    } else {
                        true_positives++;
                    }
                }
            }
            candidates.andNot(deletions);
        }
        return true_positives;
    }

    public void completeInitialisation() throws Exception {
        for( Pool<T> pool : pools ) {
            pool.completeInitialisation();
        }
    }

    /*********************************** private methods ***********************************/


    /**
     * PRE - include_list is not empty.
     * @param include_list - a list of Rings
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
