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
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Query<T> {
    public final T query;
    public final float threshold;
    private final Distance<T> validate_distance;
    private final List<Pool<Point>> pools;
    public static int total_correct_count = 0;
    public static int count = 0;
    public final ArrayList<T> real_solutions;

    /**
     * @param query
     * @param threshold
     * @param datums
     * @param validate_distance
     */
    public Query(T query, float threshold, ArrayList<T> datums, List<Pool<Point>> pools, Distance validate_distance) {
        this.query = query;
        this.threshold = threshold;
        this.validate_distance = validate_distance;
        this.pools = pools;
        real_solutions = new ArrayList<>();

        // Brute force to provide validation of algorithms - real_solutions are the real real_solutions.
        for( T p : datums ) {
            if( validate_distance.distance(p,query) < threshold ) {
                real_solutions.add(p);
            }
        }
    }

    /**
     *  Validates results for false positives and false negatives.
     *  Writes an error message if result is outwith threshold.
     * @param result
     */
    public void validate(Set<T> result) {
        count++;
        int correct_solns = 0;
        for (T potential_solution : result) {
            if (real_solutions.contains(potential_solution)) {
                correct_solns++;
            } else {
                System.err.println("FP: soln:" + potential_solution + " to query " + query + " @ " + threshold + " is not in range");
            }
        }
        if (correct_solns != real_solutions.size()) {
            int errors = real_solutions.size() - correct_solns;
            System.err.println("Queries performed = " + count + " errors = " + ( count - total_correct_count ) );
            System.err.println("FN: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs");
            ArrayList<T> clone = (ArrayList<T>) real_solutions.clone();
            clone.removeAll(result);
            for (T false_neg : clone) {
             //   System.err.println("\t\tFN:" + false_neg + " distance: " + validate_distance.distance(false_neg, query));
            }
        } else {
            total_correct_count++;
//            System.err.println("Soln to query " + query + " T: " + threshold + " is correct" );
//            System.err.print("Correct Radii: " );
//            for( float r : radii ) {
//                System.err.print( r + " ");
//            }
//            System.err.println();
        }
    }

    public void validateOmissions(Set<T> result, List<Ring<T>> include_list) {
        int correct = 0;
        int err = 0;
        for (T potential_solution : result) {
            if (real_solutions.contains(potential_solution)) {
                correct++;
            }
        }
        if (correct != real_solutions.size()) {
            int errors = real_solutions.size() - correct;
            System.err.println("Omission: FN: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs");
            System.err.print("Omission Radii: " );
            for( float r : include_list.get(0).getOwner().getRadii() ) {
                System.err.print( r + " ");
            }
            ArrayList<T>  clone = (ArrayList<T>) real_solutions.clone();
            clone.removeAll(result);
            for( T false_neg : clone) {
            //    System.err.println("\t\tOmission: FN:" + false_neg + " distance: " + validate_distance.distance(false_neg,query) );
            }
            err += errors;
        }
    }

    public void validateIncludeList(List<Ring<T>> include_list) {
        ArrayList<T>  clone = (ArrayList<T>) real_solutions.clone();
        for( Ring<T> ring : include_list ) {
            for( T point : ring.getAllContents() ) {
                clone.remove(point);
            }
        }
        if( ! clone.isEmpty() ) {
            System.err.println( "Omissions in Include list:" + clone.size() );
            for( T point : clone ) {
         //       System.err.println("\t\tOmission include: " + point );
            }
        } else {
        }
    }
}
