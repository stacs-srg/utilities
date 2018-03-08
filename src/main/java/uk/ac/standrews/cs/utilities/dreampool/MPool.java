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

import uk.ac.standrews.cs.utilities.m_tree.DataDistance;
import uk.ac.standrews.cs.utilities.m_tree.Distance;

import java.util.*;

public class MPool<T> { // aka DreamPool

    final Distance<T> distance_wrapper;
    private final Set<T> pivots;

    List<Pool<T>> pools = new ArrayList<>();

    /**
     *
     * @param d - distance wrapper
     * @param pivots
     */
    public MPool(Distance<T> d, Set<T> pivots ) throws Exception {

        this( d,pivots,null);
    }

    /**
     *
     * @param d - distance wrapper
     * @param pivots
     */
    public MPool(Distance<T> d, Set<T> pivots, float[] radii ) throws Exception {

        this.distance_wrapper = d;
        this.pivots = pivots;

        initialise(radii);
    }

    private void initialise(float[] radii) {

        for( T pivot : pivots ) {
            if( radii == null ) {
                pools.add(new Pool(pivot));
            } else {
                pools.add(new Pool(pivot,radii));
            }
        }
        check_coverage( pools );
    }

    private void check_coverage(List<Pool<T>> pools) {

    }

    public void add(T datum) throws Exception {

        for( Pool<T> pool : pools ) {
            float distance = distance_wrapper.distance( datum, pool.getPivot() );
            if( distance < pool.maxR() ) {
                pool.add(datum,distance);
            }
        }
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
    public Set<T> rangeSearch(final T query, final float threshold) { // , Query<T> query_obj) { // TODO query_obj only for validation

        List<Ring<T>> include_list = new ArrayList<>(); // circles that cover query and may contain soln
        List<Ring<T>> exclude_list = new ArrayList<>(); // circles that do not cover query and may are not part of soln

        for( Pool<T> pool : pools ) {
            float distance_query_pivot = distance_wrapper.distance(query, pool.getPivot());

            // Uses: pivot exclusion (b) For a reference point p ∈ U and any real value μ,
            // if d(q,p) ≤ μ−t, then no element of {s ∈ S | d(s,p) > μ} can be a solution to the query
            Ring r1 = pool.findIncludeRing(distance_query_pivot,threshold);
            if( r1 != null ) {
                // any circles that are added to include_list cover query.
                include_list.add(r1);
            }
            // was else { but that does not exclude inner rings!
            // TODO check and tidy format if correct
                // uses pivot exclusion (a) For a reference point p ∈ U and any real value μ,
                // if d(q,p) > μ+t, then no element of {s ∈ S | d(s,p) ≤ μ} can be a solution to the query
                Ring r2 = pool.findExcludeRing(distance_query_pivot,threshold);
                if( r2 != null ) {
                    exclude_list.add(r2); // to be refined below.
                }
            // }

        }
//        System.out.println( "Rings in range of query " + query + "threshold: " + threshold + " are:" );
//        showRings(include_list);

//        System.err.println( "Checking intersections, included pools:");
//
//        for( Ring<T> p : include_list ) {
//
//            System.err.println( "ring pivot: " + p.getOwner().getPivot() + " r max: " + p.getRmax() + " d: " + distance_wrapper.distance(query, p.getOwner().getPivot() ) );
//        }

//        query_obj.validateIncludeList(include_list);

        Set<T> candidates = intersections(include_list); //,query_obj);

//        query_obj.validateOmissions(candidates,include_list);

 //       System.out.println( "Number of inclusion candidate points: " + candidates.size() );

        int excluded = exclude( candidates, exclude_list );

//        System.out.println( "Removed " + excluded + " candidate points");

//        System.out.println( "Number of candidate points left for checking: " + candidates.size() );

        int count = filter( candidates, query, threshold );

//        System.out.println( "Number of true real_solutions to query: " + count );

        return candidates;
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
    public Set<T> rangeSearchWithHyperplane(final T query, final float threshold, Query<T> query_obj) { // TODO query_obj only for validation

        List<DataDistance<Ring<T>>> include_list = new ArrayList<>();
        List<DataDistance<Ring<T>>> exclude_list = new ArrayList<>();

        for( Pool<T> pool : pools ) {
            float distance_query_pivot = distance_wrapper.distance(query, pool.getPivot());

            // Uses: pivot exclusion (b) For a reference point p ∈ U and any real value μ,
            // if d(q,p) ≤ μ−t, then no element of {s ∈ S | d(s,p) > μ} can be a solution to the query
            Ring r1 = pool.findIncludeRing(distance_query_pivot,threshold);
            if( r1 != null ) {
                include_list.add(new DataDistance<Ring<T>>(r1,distance_query_pivot));
            }
                // was else { but that does not exclude inner rings!
                // TODO check and tidy format if correct

                // uses pivot exclusion (a) For a reference point p ∈ U and any real value μ,
                // if d(q,p) > μ+t, then no element of {s ∈ S | d(s,p) ≤ μ} can be a solution to the query
                Ring r2 = pool.findExcludeRing(distance_query_pivot,threshold);
                if( r2 != null ) {
                    exclude_list.add(new DataDistance<Ring<T>>(r2,distance_query_pivot));; // to be refined below.
                }
            // }
        }
        Set<T> hyperplane_exclude_list = performHPExclusion( exclude_list, threshold, query_obj );

//        System.out.println( "Rings in range of query " + query + "threshold: " + threshold + " are:" );
//        showRings(include_list);

//        System.err.println( "Checking intersections, included pools:");
//
//        for( Ring<T> p : include_list ) {
//
//            System.err.println( "ring pivot: " + p.getOwner().getPivot() + " r max: " + p.getRmax() + " d: " + distance_wrapper.distance(query, p.getOwner().getPivot() ) );
//        }

//        query_obj.validateIncludeList(include_list);

        Set<T> candidates = intersections( extractTsFromDDs( include_list ) ); // TODO fix later - decide on DD or T. // include_list); //,query_obj);

//        query_obj.validateOmissions(candidates,include_list);

        //       System.out.println( "Number of inclusion candidate points: " + candidates.size() );

        int excluded = exclude( candidates, extractTsFromDDs( exclude_list ) ); // TODO fix later - decide on DD or T.

//        System.out.println( "Removed " + excluded + " candidate points");

//        System.out.println( "Number of candidate points left for checking: " + candidates.size() );

        int count = filter( candidates, query, threshold );

//        System.out.println( "Number of true real_solutions to query: " + count );

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
     * hyperplane exclusion For reference points p1, p2 ∈ U , if d(q, p1) − d(q, p2) > 2t,
     * then no element of {s ∈ S | d(s,p1) ≤ d(s,p2)} can be a solution to the query
     */
    private Set<T> performHPExclusion(List<DataDistance<Ring<T>>> pivots_and_distances, float threshold, Query<T> query_obj) {

        Set<T> hp_exclude_set = new HashSet<>(); // to do make this filter the include set.

        for( int index1 = 0; index1 < pivots_and_distances.size() - 1; index1++ ) {
            for( int index2 = index1 + 1; index2 < pivots_and_distances.size(); index2++ ) {

                DataDistance<Ring<T>> dd1 = pivots_and_distances.get(index1);
                Ring<T> r1 = dd1.value;
                DataDistance<Ring<T>> dd2 = pivots_and_distances.get(index2);
                Ring<T> r2 = pivots_and_distances.get(index2).value;

                if( r1.getRing_number() != r2.getRing_number() ) {

                    // no point in looking if rings are the same size.

                    float distance1 = dd1.distance;
                    float distance2 = dd2.distance;

                    do_hp_exclusion(threshold, query_obj, hp_exclude_set, r1, distance1, r2, distance2);
                    do_hp_exclusion(threshold, query_obj, hp_exclude_set, r2, distance2, r1, distance1);
                }
            }

        }
        return hp_exclude_set;
    }

    private void do_hp_exclusion(float threshold, Query<T> query_obj, Set<T> hp_exclude_set, Ring<T> r1, float distance1, Ring<T> r2, float distance2) {

        if( distance1 - distance2 > ( 2 * threshold ) ) {

            System.err.println( "HP" );

            // d(s,p1) ≤ d(s,p2)}
            if( r1.getRmax() < r2.getRmin() ) {
                System.err.println( "Min" );
                ArrayList<T> elements2 = r2.getAllContents();
                for( T element : r1.getAllContents() ) {
                    if( elements2.contains(element) ) {
                        hp_exclude_set.add(element);
                        if( query_obj.real_solutions.contains(element)) {
                            System.err.println( "Illegal HP exclusion for: " + element );
                        } else {
                            System.err.println( "Added HP exclusion for: " + element );
                        }

                    } else {
                        System.err.println( "X" );
                    }
                }
            }

        }
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

    private Set<T> intersections(List<Ring<T>> include_list) { // }, Query<T> query_obj) {  // TODO query_obj only for debug
        if( include_list.isEmpty() ) {
            return null;
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
                    result = intersection(result, remaining_rings.getAllContents()); // ,query_obj );
                }
            }
            return result;
        }
    }

    private int exclude(Set<T> candidates, List<Ring<T>> exclude_list) {
        int count = 0;
        for( Ring<T> ring : exclude_list ) {
            ArrayList<T> ring_contents = ring.getAllContents();
            for ( T candidate : ring_contents ) {
                if( candidates.remove(candidate) ) {
                    count++;
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

        for( T candidate : candidates ) {
            distance_calcs++;

            if ( distance_wrapper.distance( query,candidate ) > threshold ) {
                dropset.add(candidate);     // How do you write this without ConcurrentModificationException ??
                false_positives++;
            } else {
                true_positives++;
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


    public Set<T> intersection(Collection<T> a, Collection<T> b) { // }, Query<T> query_obj) { // TODO query_obj only for debug
        Set<T> result = new HashSet<>();
        for( T next : a ) {
            if( b.contains( next ) ) {
                result.add( next );
            }
// was for debug:
//            else {
//                if( query_obj.real_solutions.contains(next)) {
//                    System.err.println( "Eliminating point that should be part of soln space: " + next );
//
//                }
//            }
        }
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
