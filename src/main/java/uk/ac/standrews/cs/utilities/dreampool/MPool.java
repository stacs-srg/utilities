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

import java.util.*;

/**
 * A Metric datastructure based on pivot exclusion and hyperplane exclusion.
 *
 * @param <T> The type of the elements stored in this data structure.
 * @author al@st-andrews.ac.uk
 */
public class MPool<T> {

    private final List<T> pivots;                            // the set of all pivots in the system.
    private int num_pivots;                                  // number of pivots (=number of pools) in the implementation
    private final Distance<T> distance_wrapper;             // the distance function used in the implementation.
    private int element_id = 0;                             // an int used to represent each of the elements used in s, used to index values TreeMap.

    private double[][] inter_pivot_distances;
    List<Pool<T>> pools = new ArrayList<>();                // the set of pools containing rings and pivots.
    private List<T> values = new ArrayList<>();             // used to store mappings from indices to the elements of s (to facilitate lookup from bitmaps to datums).
    private int num_values;                                 // number of values in values

    boolean fourPoint = true;  // TODO should be a parameter


//    private Ring<T> universal_ring =  // TODO why do we not need this?
//            new Ring<T>(null,
//                        this,
//                        0,
//                        0.0f,
//                        1.0F,
//                        null );                    // a ring containing all the objects in the universe (this is a catch all to ensure coverage of all points)

    /**
     * Create MPool using specified radii
     *
     * @param distance_wrapper - a distance function that determines distance between two Ts
     * @param pivots           - a set of pivots/reference objects to use.
     * @param radii            - a set of radii to form pools (balls) around the pivots.
     */
    public MPool(Distance<T> distance_wrapper, List<T> pivots, double[] radii) throws Exception {
        this.pivots = pivots;
        this.distance_wrapper = distance_wrapper;
        this.num_pivots = pivots.size();

        initialise_inter_pivot_distances(pivots);
        initialisePools(radii, distance_wrapper);
    }


    public void add(T datum) throws Exception {

        double[] distances_from_datum_to_pivots = new double[num_pivots];
        int pivot_index = 0;

        values.add(datum);                    // add to the master index to facilitate lookup from bitmaps to datums.
        for (Pool<T> pool : pools) {           // calculate the distances to all the pivots.
            distances_from_datum_to_pivots[pivot_index++] = distance_wrapper.distance(pool.pivot, datum);
        }
        for (Pool<T> pool : pools) {
            pool.add(element_id, distances_from_datum_to_pivots);
        }

        // universal_ring.add(element_id);
        element_id++;
    }

    private void initialisePools(double[] radii, Distance<T> distance_wrapper) throws Exception {

        int pool_id = 0;

        for (T pivot : pivots) {
            pools.add(new Pool(pivot, pool_id, num_pivots, radii));
            pool_id++;
        }

    }

    /**
     * @param pivots - the set of pivots being used in this instance
     *               <p>
     *               initialises an 2D array of inter pivot distances called inter_pivot_distances
     */
    private void initialise_inter_pivot_distances(List<T> pivots) {
        inter_pivot_distances = new double[num_pivots][num_pivots];
        int i = 0;
        for (T p1 : pivots) {
            int j = 0;
            for (T p2 : pivots) {
                double d = distance_wrapper.distance(p1, p2);
                inter_pivot_distances[i][j] = d;
                inter_pivot_distances[j][i] = d;
                j++;
            }
            i++;
        }
    }

    /**
     * Find the nodes within range r of query.
     *
     * @param query     - some data for which to find the neighbours within the distance specified by threshold
     * @param threshold - the threshold distance specifying the size of the query ball
     *                  //     * @param query_obj - the query being performed (for diagnostics) - permits extra data to be passed in and out and validation
     * @return all those nodes from S within @param threshold
     * <p>
     * General technique - find the rings that overlap with the query then do inclusion,
     * find the rings that do not overlap using and do exclusion,
     * finally perform hyperplane exclusion,
     * filter the results to exclude false positives.
     */
    public List<T> rangeSearch(final T query, final double threshold ) {  // NOTE query_obj only for validation

        List<BitSet> mustBeIn = new ArrayList<>();
        List<BitSet> cantBeIn = new ArrayList<>();

        List<T> results = new ArrayList<>();

        double[] distances_from_query_to_pivots = initialiseQueryToPivotDistances(query, threshold, results);

        excludeHyperplanePartitions(threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        excludeRadiusPartitions(threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        doExclusions(threshold, query, results, mustBeIn, cantBeIn, null );

        return results;
    }

    public List<T> diagnosticRangeSearch(final T query, final double threshold, Query<T> query_obj) {

        int partitionsExcluded = 0;

        List<BitSet> mustBeIn = new ArrayList<>();
        List<BitSet> cantBeIn = new ArrayList<>();

        List<T> results = new ArrayList<>();

        double[] distances_from_query_to_pivots = initialiseQueryToPivotDistances(query,threshold,results);
        int distance_index = 0;


        //********** TODO Put reference objects into result set here.

        excludeHyperplanePartitions(threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        long hp_exclusions = calcsize( cantBeIn );
        long hp_inclusions = calcsize( mustBeIn );

        query_obj.setHPexclusions( hp_exclusions );
        query_obj.setHPinclusions( hp_inclusions );

        excludeRadiusPartitions(threshold, distances_from_query_to_pivots, mustBeIn, cantBeIn);

        query_obj.setPivotExclusions( calcsize( cantBeIn ) - hp_exclusions );
        query_obj.setPivotInclusions( calcsize( mustBeIn ) - hp_inclusions );

        doExclusions(threshold, query, results, mustBeIn, cantBeIn,query_obj);

        return results;

    }


    private double[] initialiseQueryToPivotDistances(T query, double threshold, List<T> results) {
        double[] distances = new double[num_pivots];

        for (int i = 0; i < num_pivots; i++) {
            Pool<T> pool = pools.get(i);
            T pivot = pool.pivot;
            double distance_query_pivot = distance_wrapper.distance(pivot, query);
            distances[i] = distance_query_pivot;
            if (distance_query_pivot < threshold) {
                results.add(pivot);
            }
        }
        return distances;
    }

    private static long calcsize(List<BitSet> bits) {
        long result = 0l;
        for (BitSet bs : bits) {
            result += bs.cardinality();
        }
        return result;
    }

    private void doExclusions(double threshold, T query, List<T> results,
                              List<BitSet> mustBeIn, List<BitSet> cantBeIn, Query<T> query_obj) {
        if (mustBeIn.size() != 0) {
            BitSet ands = getAndBitSets(mustBeIn);
            if (cantBeIn.size() != 0) {
                /*
                 * hopefully the normal situation or we're in trouble!
                 */
                BitSet nots = getOrBitSets(cantBeIn);
                nots.flip(0, num_values);
                ands.and(nots);
                if( query_obj != null ) { query_obj.setRequiringFiltering( ands.cardinality() ); }
                filterContenders(threshold, query, results, ands);
            } else {
                // there are no cantBeIn partitions
                if( query_obj != null ) { query_obj.setRequiringFiltering( ands.cardinality() ); }
                filterContenders(threshold, query, results, ands);
            }
        } else {
            // there are no mustBeIn partitions
            if (cantBeIn.size() != 0) {
                BitSet nots = getOrBitSets(cantBeIn);
                nots.flip(0, num_values);
                if( query_obj != null ) { query_obj.setRequiringFiltering( nots.cardinality() ); }
                filterContenders(threshold, query, results, nots);
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


    private void excludeHyperplanePartitions(double threshold, double[] distances_from_query_to_pivots,
                                             List<BitSet> mustBeIn, List<BitSet> cantBeIn) {
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


    private void excludeRadiusPartitions(double threshold, double[] distances_from_query_to_pivots,  // TODO generalise these properly.
                                         List<BitSet> mustBeIn, List<BitSet> cantBeIn) {
        for (int pivot_index = 0; pivot_index < num_pivots; pivot_index++) {
            double distance_query_pivot = distances_from_query_to_pivots[pivot_index];

            Pool<T> pool = pools.get(pivot_index);
            Ring<T>[] rings = pool.rings;

            // Inclusion first

            for (int i = 0; i < rings.length; i++) {
                if (distance_query_pivot < pool.rings[i].radius - threshold) {
                    mustBeIn.add(pool.rings[i].contents);
                    break; // stop once we have found once that covers
                }
            }

            // Next exclusion - do in reverse order

            for (int i = rings.length - 1; i >= 0; i--) {
                if (distance_query_pivot < pool.rings[i].radius - threshold) {
                    mustBeIn.add(pool.rings[i].contents);
                    break; // stop once we have the smallest that doesn't cover
                }
            }
        }
    }

    private void filterContenders(double threshold, T query, List<T> results, BitSet candidates) {

        for (int i = candidates.nextSetBit(0); i >= 0; i = candidates.nextSetBit(i + 1)) {
            if (distance_wrapper.distance(query, values.get(i)) < threshold) {
                results.add(values.get(i));
            }
        }
    }

    private BitSet getAndBitSets(List<BitSet> mustBeIn) {
        BitSet ands = null;
        if (mustBeIn.size() != 0) {
            ands = mustBeIn.get(0).get(0, num_values); // a new copy
            for (int i = 1; i < mustBeIn.size(); i++) {
                ands.and(mustBeIn.get(i));
            }
        }
        return ands;
    }

    private BitSet getOrBitSets(List<BitSet> cantBeIn) {
        BitSet nots = null;
        if (cantBeIn.size() != 0) {
            nots = cantBeIn.get(0).get(0, num_values); // a new copy
            for (int i = 1; i < cantBeIn.size(); i++) {
                nots.or(cantBeIn.get(i));
            }
        }
        return nots;
    }

    /**
     * @param i - an index into the set of datums
     * @return the ith datum in the set
     */
    public T getValue(int i) {
        return values.get(i);
    }

    /**
     * @param candidates - a bitmap representing a set of values drawn from S
     * @return the set of values of type T which are represented by the bitmap
     */
    public Set<T> getValues(BitSet candidates) {
        Set<T> result = new HashSet<>();

        for (int i = 0; i < num_values; i++) {
            if (candidates.get(i)) {
                result.add(getValue(i));
            }
        }

        return result;
    }

    public void completeInitialisation() throws Exception {
        num_values = values.size();
    }

}
