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

import java.util.*;

/**
 * @author al@st-andrews.ac.uk
 * @param <T> The type of the elements stored in this data structure.
 *
 */
public class MPool<T> {

    private final Set<T> pivots;                            // the set of all pivots in the system.
    private int num_pools;                                  // number of pools (=number of pivots) in the implementation
    private final Distance<T> distance_wrapper;             // the distance function used in the implementation.

    List<Pool<T>> pools = new ArrayList<>();                // the set of pools containing rings and pivots.
    private int element_id = 0;                             // an int used to represent each of the elements used in s, used to index values TreeMap.
    private List<T> values = new ArrayList<>();             // used to store mappings from indices to the elements of s (to facilitate lookup from bitmaps to datums).

    private float[][] inter_pivot_distances;

    private Ring<T> universal_ring =
            new Ring<T>(null,
                        this,
                        0,
                        0.0f,
                        1.0F,
                        null );                    // a ring containing all the objects in the universe (this is a catch all to ensure coverage of all points)


    /**
     * Create MPool using specified radii
     * @param pivots
     * @param radii
     *
     */
    public MPool(Distance<T> distance_wrapper, Set<T> pivots, float[] radii) throws Exception {
        this.pivots = pivots;
        this.distance_wrapper = distance_wrapper;
        this.num_pools = pivots.size();

        initialise_pivot_distances( pivots );
        if( radii == null ) {
            radii = calculateRadii(inter_pivot_distances);
        }
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
    public MPool(Distance<T>  distance, Set<T> ros) throws Exception {
        this( distance, ros, Pool.DEFAULT_RADII );
    }


    /**
     * Calculates the radii of the pools based on the median inter-pivot distance +/- 3 std devs
     * @param distances - the distances from all pivots to the datums.
     * @return a crafted set of radii - { median - 3sd,median - 2sd,median - sd,median,median + sd,median + 2sd,median + 3sd }
     */
    private static float[] calculateRadii(float[][] distances) {

        double mean = mean( distances );
        double sd = stdDev( distances, mean );

        System.out.println( "mean: " + mean);
        System.out.println( "sd: " + sd);

        double sd_times_point_5 = 0.5 * sd;
        double sd_times_1_point_5 = 1.5 * sd;
        double sd_times_2 = 2 * sd;
        double sd_times_2_point_5 = 2.5 * sd;

        float[] radii = new float[]{
//                (float)(((float) mean) - sd_times_2_point_5), // TODO look at this skewed distribution.
//                (float)(((float) mean) - sd_times_2),
                (float)(((float) mean) - sd_times_1_point_5),
                (float)(((float) mean) - sd),
                (float)(((float) mean) - sd_times_point_5),
                (float) mean,
                (float)(((float) mean) + sd_times_point_5),
                (float)(((float) mean) + sd),
                (float)(((float) mean) + sd_times_1_point_5),
                (float)(((float) mean) + sd_times_2),
                (float)(((float) mean) + sd_times_2_point_5),
        };

        return radii;
    }

    /**
     * @param arrai
     * @param mean
     * @return
     */
    private static double stdDev( float[][] arrai, double mean ) {
        double sum = 0;
        int count = 0;

        for (int i = 0; i < arrai.length - 1; i++) {
            for (int j = i; j < arrai[i].length; j++) {
                double difference = arrai[i][j] - mean;
                sum = sum + difference * difference;
                count++;
            }
        }
        double squaredDiffMean = (sum) / count;
        double standardDev = (Math.sqrt(squaredDiffMean));

        return standardDev;
    }

    public static double mean(float[][] arrai) {
        double sum = 0;                             // sum of all the distances between elements
        int count = 0;

        for (int i = 0; i < arrai.length -1; i++) {
            for( int j = i; j < arrai[i].length; j++ ) {
                sum += arrai[i][j];
                count++;
            }
        }
        return sum / count;
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
    private void initialise_pivot_distances(Set<T> pivots) {
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
     *
     * @param i - the ith pivot
     * @param j - the jth pivot
     * @return the distance( pivot i, pivot j )
     */
    public float getInterPivotDistance(int i, int j) {
        return inter_pivot_distances[i][j];
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
    public Set<T> rangeSearch(final T query, final float threshold, Query<T> query_obj ) { // NOTE query_obj only for validation

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

            /** Next perform hyperplane exclusion: For a reference point pi ∈ U,
             ** If d(q,p1) - d(q,p2) > 2t, then no element of {s ∈ S | d(s,p1) ≤ d(s,p2) } can be a solution to the query
             ** Here we are performing the first part of this - d(s,p1) ≤ d(s,p2), first second part evaluated at initialisation time.
             **/

            exclusions = pool.findHPExclusion4P(exclusions, distances_from_query_to_pivots, threshold);
            query_obj.validateHPExclusions(exclusions);
        }

        int hp_exclusions = exclusions.getCardinality();
        query_obj.setHPexclusions( hp_exclusions );

        include_list.add( universal_ring );
        query_obj.validateIncludeList(include_list, query_obj);
        RoaringBitmap inclusions = intersections(include_list,query_obj);

        int pivot_inclusions = inclusions.getCardinality();
        query_obj.setPivotInclusions( pivot_inclusions );

        query_obj.validateOmissions(inclusions,include_list);

        exclusions = exclude( exclusions, exclude_list );

        int pivot_exclusions = exclusions.getCardinality();
        query_obj.setPivotExclusions( pivot_exclusions );

        inclusions.andNot(exclusions); // was inclusions = inclusions.difference

        query_obj.setRequiringFiltering( inclusions.getCardinality() );

        int count = filter( inclusions, query, threshold );

        return getValues( inclusions );
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

    private RoaringBitmap exclude(RoaringBitmap exclusions, List<Ring<T>> exclude_list) { //***************************************

        for( Ring<T> ring : exclude_list ) {
            RoaringBitmap ring_contents = ring.getConciseContents();

            exclusions.or(ring_contents); // was exclusions = exclusions.difference(ring_contents);

        }
        return exclusions;
    }

    private int filter(RoaringBitmap candidates, T query, float threshold) {
        int false_positives = 0;
        int true_positives = 0;

        Set<Integer> dropset = new HashSet<>();

        if (candidates != null && !candidates.isEmpty()) {

            Iterator<Integer> iter = candidates.iterator();

            while (iter.hasNext()) {

                int next = iter.next();
                T candidate = values.get(next);
                if (candidate != null) {
                    if (distance_wrapper.distance(query, candidate) > threshold) {
                        dropset.add(next); // TODO this is not necessary!! ****** FIXME
                        false_positives++;
                    } else {
                        true_positives++;
                    }
                }
            }
            for (int member : dropset) {
                candidates.flip(member);  // TODO this is not necessary!! ****** FIXME
            }

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
