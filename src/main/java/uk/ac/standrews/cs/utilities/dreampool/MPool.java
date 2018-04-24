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
 *
 * A Metric datastructure based on pivot exclusion and hyperplane exclusion.
 * @author al@st-andrews.ac.uk
 * @param <T> The type of the elements stored in this data structure.
 *
 */
public class MPool<T> {

    private final List<T> pivots;                            // the set of all pivots in the system.
    private int num_pivots;                                  // number of pivots (=number of pools) in the implementation
    private final Distance<T> distance_wrapper;             // the distance function used in the implementation.

    List<Pool<T>> pools = new ArrayList<>();                // the set of pools containing rings and pivots.
    private int element_id = 0;                             // an int used to represent each of the elements used in s, used to index values TreeMap.
    private List<T> values = new ArrayList<>();             // used to store mappings from indices to the elements of s (to facilitate lookup from bitmaps to datums).
    private int num_values;                                 // number of values in values

    private Ring<T> universal_ring =
            new Ring<T>(null,
                        this,
                        0,
                        0.0f,
                        1.0F,
                        null );                    // a ring containing all the objects in the universe (this is a catch all to ensure coverage of all points)
    private float[][] inter_pivot_distances;

    boolean fourPoint = true;  // TODO should be a parameter


    /**
     * Create MPool using specified radii
     * @param distance_wrapper - a distance function that determines distance between two Ts
     * @param pivots - a set of pivots/reference objects to use.
     * @param radii - a set of radii to form pools (balls) around the pivots.
     */
    public MPool(Distance<T> distance_wrapper, List<T> pivots, float[] radii) throws Exception {
        this.pivots = pivots;
        this.distance_wrapper = distance_wrapper;
        this.num_pivots = pivots.size();

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

        float[] distances_from_datum_to_pivots = new float[num_pivots];
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
            pools.add(new Pool(pivot, pool_id, num_pivots, radii, this, distance_wrapper));
            pool_id++;
        }

    }

    /**
     * @param pivots - the set of pivots being used in this instance
     *
     * initialises an 2D array of inter pivot distances called inter_pivot_distances
     */
    private void initialise_pivot_distances(List<T> pivots) {
        inter_pivot_distances = new float[num_pivots][num_pivots];
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
    public List<T> rangeSearch(final T query, final float threshold, Query<T> query_obj) { // NOTE query_obj only for validation

        RoaringBitmap pivot_inclusions  = universal_ring.getConciseContents().clone();
        RoaringBitmap pivot_exclusions = new RoaringBitmap();

        int noOfResults = 0;
        int partitionsExcluded = 0;

        float[] distances_from_query_to_pivots = new float[num_pivots];
        int distance_index = 0;

        for (Pool<T> pool : pools) {
            float distance_query_pivot = distance_wrapper.distance(pool.getPivot(), query);
            distances_from_query_to_pivots[distance_index++] = distance_query_pivot;              // save this for HP exclusion - used below.
        }

        List<RoaringBitmap> mustBeIn = new ArrayList<>();
        List<RoaringBitmap> cantBeIn = new ArrayList<>();

        List<T> results = new ArrayList<>();

        //System.out.println( "Query " );
        excludeHyperplanePartitions( threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        //System.out.println( "\tHP mustBeIn " + mustBeIn.size() + " cantBeIn " + cantBeIn.size() );

        partitionsExcluded += cantBeIn.size() + mustBeIn.size();

        excludeRadiusPartitions(threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        //System.out.println( "\tRadius mustBeIn " + mustBeIn.size() + " cantBeIn " + cantBeIn.size() );

        doExclusions(threshold, query, results, mustBeIn, cantBeIn);

        //System.out.println( "\tResults " + results.size() );

        noOfResults += results.size();

        query_obj.checkSolutions(results);

        return results;

//        query_obj.setHPexclusions( pivot_exclusions.getCardinality() - pe_before );
//        query_obj.validateHPExclusions(pivot_exclusions);
//        query_obj.validateOmissions(pivot_inclusions);

    }

    private void doExclusions(double threshold, T query, List<T> results,
                                     List<RoaringBitmap> mustBeIn, List<RoaringBitmap> cantBeIn) {
        if (mustBeIn.size() != 0) {
            RoaringBitmap ands = getAndBitSets(mustBeIn);
            if (cantBeIn.size() != 0) {
                /*
                 * hopefully the normal situation or we're in trouble!
                 */
                RoaringBitmap nots = getOrBitSets(cantBeIn);
                nots.flip((long)0, (long) values.size());
                ands.and(nots);
                filterContendors(threshold, query, results, ands);
            } else {
                // there are no cantBeIn partitions
                filterContendors(threshold, query, results, ands);
            }
        } else {
            // there are no mustBeIn partitions
            if (cantBeIn.size() != 0) {
                RoaringBitmap nots = getOrBitSets(cantBeIn);
                nots.flip((long)0, (long) values.size());
                filterContendors(threshold, query, results, nots);
            } else {
                // there are no exclusions at all...
                for (T d : values) {
                    if (distance_wrapper.distance(query, d) < threshold) {
                        results.add(d);
                    }
                }
            }
        }
    }


    private void excludeHyperplanePartitions(float threshold, float[] distances_from_query_to_pivots,
                                                    List<RoaringBitmap> mustBeIn, List<RoaringBitmap> cantBeIn) {
        for (int i = 0; i < num_pivots - 1; i++) {
            double d1 = distances_from_query_to_pivots[i];
            for (int j = i + 1; j < num_pivots; j++) {
                double d2 = distances_from_query_to_pivots[j];

                boolean cond1 = false;
                if (fourPoint) {
                    cond1 = (d2 * d2 - d1 * d1) / inter_pivot_distances[i][j] > 2 * threshold;
                } else {
                    cond1 = (d2 - d1) > 2 * threshold;
                }
                if (cond1) {
                    mustBeIn.add(pools.get(i).closer_than[j]);
                } else {
                    boolean cond2 = false;
                    if (fourPoint) {
                        cond2 = (d1 * d1 - d2 * d2) / inter_pivot_distances[i][j] > 2 * threshold;
                    } else {
                        cond2 = (d1 - d2) > 2 * threshold;
                    }
                    if (cond2) {
                        cantBeIn.add(pools.get(i).closer_than[j]);
                    }
                }
            }
        }
    }


    private void excludeRadiusPartitions( float threshold, float[] distances_from_query_to_pivots,
                                                 List<RoaringBitmap> mustBeIn, List<RoaringBitmap> cantBeIn) {
        for (int i = 0; i < num_pivots; i++) {
            double distance_query_pivot = distances_from_query_to_pivots[i];

            Pool<T> pool = pools.get(i);
            if (distance_query_pivot < pool.radii[0] - threshold ) {
                mustBeIn.add( pool.getRing(0).getConciseContents() );
            } else if (distance_query_pivot < pool.radii[1] - threshold ) {
                mustBeIn.add( pool.getRing(1).getConciseContents() );
            } else if (distance_query_pivot < pool.radii[2] - threshold ) {
                mustBeIn.add( pool.getRing(2).getConciseContents() );
            }

            if (distance_query_pivot >= threshold + pool.radii[0]  ) {
                cantBeIn.add( pool.getRing(0).getConciseContents() );
            } else if (distance_query_pivot >= threshold + pool.radii[1]) {
                cantBeIn.add( pool.getRing(1).getConciseContents() );
            } else if (distance_query_pivot >= threshold + pool.radii[2]) {
                cantBeIn.add( pool.getRing(2).getConciseContents() );
            }
        }
    }

    private void filterContendors(double threshold, T query, List<T> results, RoaringBitmap candidates) {

        Iterator<Integer> iter = candidates.iterator();

        while (iter.hasNext()) {

            int next = iter.next();

            if (distance_wrapper.distance(query, values.get(next)) < threshold) {
                results.add(values.get(next));
            }
        }
    }

    private RoaringBitmap getAndBitSets(List<RoaringBitmap> mustBeIn) {
        if (mustBeIn.size() != 0) {
            RoaringBitmap ands = mustBeIn.get(0).clone();
            for (int i = 1; i < mustBeIn.size(); i++) {
                ands.and(mustBeIn.get(i));
            }
            return ands;
        } else {
            return new RoaringBitmap(); // there are no inclusions.
        }

    }

    private static RoaringBitmap getOrBitSets(List<RoaringBitmap> cantBeIn) {
        RoaringBitmap nots = new RoaringBitmap();
        if (cantBeIn.size() != 0) {
            for (int i = 0; i < cantBeIn.size(); i++) {
                nots.or(cantBeIn.get(i));
            }
        }
        return nots;
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

    public void completeInitialisation() throws Exception {
        for( Pool<T> pool : pools ) {
            pool.completeInitialisation();
        }
        num_values = values.size();
    }

}
