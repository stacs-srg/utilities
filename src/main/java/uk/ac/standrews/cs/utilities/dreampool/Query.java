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

public class Query<T> {
    public final T query;
    public final float threshold;
    private final Distance<T> validate_distance;
    private final List<Pool<T>> pools;
    public static int total_correct_count = 0;
    public static int count = 0;
    public final ArrayList<T> real_solutions;
    private final boolean validate;
    private final MPool<T> owner;

    private long count_hp_exclusions = 0;
    private long count_hp_inclusions = 0;
    private long count_pivot_inclusions = 0;
    private long count_pivot_exclusions = 0;
    private long requiring_filtering = 0;

    /**
     * @param query
     * @param threshold
     * @param datums
     * @param validate_distance
     */
    public Query(T query, MPool<T> owner, float threshold, List<T> datums, List<Pool<T>> pools, Distance<T> validate_distance, boolean validate) {
        this.query = query;
        this.owner = owner;
        this.threshold = threshold;
        this.validate_distance = validate_distance;
        this.pools = pools;
        this.validate = validate;
        real_solutions = new ArrayList<>();  // TODO make this a concise set like the others!

        if( validate ) {
            // Brute force to provide validation of algorithms - real_solutions are the real real_solutions.
            for (T p : datums) {
                if (validate_distance.distance(p, query) <= threshold) {
                    real_solutions.add(p);
                }
            }
        }
    }

    /**
     *  Validates results for false positives and false negatives.
     *  Writes an error message if result is outwith threshold.
     * @param result
     */
    public void validate(List<T> result) {
        if( validate ) {
            count++;
            int correct_solns = 0;
            if (result != null && !result.isEmpty()) {
                for (T potential_solution : result) {
                    if (real_solutions.contains(potential_solution)) {
                        correct_solns++;
                    } else {
                        System.out.println("FP: soln:" + potential_solution + " to query " + query + " @ " + threshold + " is not in range, distance = " + validate_distance.distance(query,potential_solution) );
                    }
                }

                if (correct_solns != real_solutions.size()) {
                    System.out.println("!!! Correct = " + correct_solns + " real = " + real_solutions.size() + " result = " + result.size() );
                    int errors = real_solutions.size() - correct_solns;
                    System.out.println("Queries performed = " + count + " errors = " + (count - total_correct_count));
                    System.out.println("FN: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs");

                    ArrayList<T> clone = (ArrayList<T>) real_solutions.clone();
                    System.out.println("%%%" + clone.size() );
                    clone.removeAll(result);
                    System.out.println("*** " + clone.size() );
                    for( T value : clone ) {
                        System.out.println("\t +++ Missing = " + value + " at distance " + validate_distance.distance(value,query) );
                    }

                } else {
                    System.out.println("QOK: " + correct_solns );
                }
            } else {
                if( real_solutions.size() == 0 ) {
                    System.out.println("QOK: " + correct_solns);
                    total_correct_count++;
                } else {
                    System.out.println("Q empty - error: " + real_solutions.size() + " missing" );

                }
            }
        }
    }

    public void validateOmissions(BitSet result) {
        if( validate ) {

            Set<T> results = owner.getValues(result);

            int correct = 0;
            int err = 0;
            for (T proposed_solution : results) {
                if (real_solutions.contains(proposed_solution)) {
                    correct++;
                }
            }
            if (correct != real_solutions.size()) {
                int errors = real_solutions.size() - correct;
                System.out.println("validateOmissions: Omission: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs, real solutions = " + real_solutions.size() + " correct = " + correct );


                Set<T> clone = owner.getValues(result); // used to be a real clone but can now get the list again.
                clone.removeAll(results);
                for (T false_neg : clone) {
                        System.out.println("\t\tOmission: FN:" + false_neg + " distance: " + validate_distance.distance(false_neg,query) );
                }
                err += errors;
            }
        }
    }

    public void validateIncludeList(Collection<Ring<T>> include_list, Query<T> query_obj) {
        if (validate) {
            ArrayList<T> clone = new ArrayList<T>();
            clone.addAll( real_solutions );
            for (Ring<T> ring : include_list) {
                for (T point : owner.getValues( ring.contents ) ) {
                    clone.remove(point);
                }
            }
            if (!clone.isEmpty()) {
                System.out.println("validateIncludeList: Omissions in Include list:" + clone.size());
                System.out.println("validateIncludeList: Include list size = " + include_list.size() );
                int records_in_soln = 0;
                for( Ring<T> ring : include_list ) {
                    records_in_soln += ring.size();
                }
                System.out.println("validateIncludeList: Include list solution size = " + records_in_soln );
            }
        }
    }

    public void checkSolutions(List<T> result) {
        int correct_solns = 0;
        boolean errors = false;
        if (result != null && !result.isEmpty()) {
            for (T potential_solution : result) {

                float d = validate_distance.distance( potential_solution,query );

                if( d > threshold ) {
                    System.out.println("checkSolutions: Error: soln to query " + query + " at distance = " + d + " > threshold " + threshold );
                    errors = true;
                } else {
                    correct_solns++;
                }
            }
        }
        if( ! errors ) {
            System.out.println("QOK " + correct_solns );
        }
    }

    public void validateHPExclusions(RoaringBitmap values) {
        if( validate ) {

            int error_count = 0;

            Iterator<Integer> iter = values.iterator();

            while( iter.hasNext() ) {

                T value = owner.getValue(iter.next());
                if (real_solutions.contains(value)) {
                    System.out.println("validateHPExclusions: value: " + value + " wrongly excluded" );
                    error_count++;
                }
            }
//            if( error_count == 0 ) {
//                System.out.println("validateHPExclusions: OK" );
//            } else {
//                System.out.println("validateHPExclusions: errors = " + error_count );
//            }

        }

    }

    public void setHPexclusions(long HPexclusions) {
        this.count_hp_exclusions = HPexclusions;
    }

    public void setHPinclusions(long HPinclusions ) {
        this.count_hp_inclusions = HPinclusions;
    }

    public void setPivotInclusions(long pivotInclusions) {
        this.count_pivot_inclusions = pivotInclusions;
    }

    public void setPivotExclusions(long pivotExclusions) {
        this.count_pivot_exclusions = pivotExclusions;
    }

    public void setRequiringFiltering(int requiringFiltering) {
        this.requiring_filtering = requiringFiltering;
    }

    public long getHPExclusions() {
        return count_hp_exclusions;
    }

    public long getHPInclusions() {
        return count_hp_inclusions;
    }


    public long getPivotInclusions() {
        return count_pivot_inclusions;
    }

    public long getPivotExclusions() {
        return count_pivot_exclusions;
    }

    public long getRequiringFiltering() {
        return requiring_filtering;
    }

}
