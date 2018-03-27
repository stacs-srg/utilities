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
    private final boolean validate;

    /**
     * @param query
     * @param threshold
     * @param datums
     * @param validate_distance
     */
    public Query(T query, float threshold, ArrayList<T> datums, List<Pool<Point>> pools, Distance validate_distance, boolean validate) {
        this.query = query;
        this.threshold = threshold;
        this.validate_distance = validate_distance;
        this.pools = pools;
        this.validate = validate;
        real_solutions = new ArrayList<>();

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
    public void validate(Set<T> result) {
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
                    int errors = real_solutions.size() - correct_solns;
                    System.out.println("Queries performed = " + count + " errors = " + (count - total_correct_count));
                    System.out.println("FN: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs");
                    ArrayList<T> clone = (ArrayList<T>) real_solutions.clone();
                    clone.removeAll(result);
                } else {
                    System.out.println("QOK: " + correct_solns );
                }
            } else {
                if( real_solutions.size() == 0 ) {
                    System.out.println("QOK: " + correct_solns);
                    total_correct_count++;
                } else {
                    System.out.println("Q empty - error: " + real_solutions.size() + " missing" );
//                    for( T soln : real_solutions ){
//
//                    }

                }
            }
        }
    }

    public void validateOmissions(Set<T> result, List<Ring<T>> include_list) {
        if( validate ) {
            int correct = 0;
            int err = 0;
            for (T potential_solution : result) {
                if (real_solutions.contains(potential_solution)) {
                    correct++;
                }
            }
            if (correct != real_solutions.size()) {
                int errors = real_solutions.size() - correct;
                System.out.println("validateOmissions: Omission: soln to query " + query + " T: " + threshold + " contains " + errors + " FNs, real solutions = " + real_solutions.size() + " correct = " + correct );


                ArrayList<T> clone = (ArrayList<T>) real_solutions.clone();
                clone.removeAll(result);
                for (T false_neg : clone) {
                        System.out.println("\t\tOmission: FN:" + false_neg + " distance: " + validate_distance.distance(false_neg,query) );
                }
                err += errors;
            }
        }
    }

    public void validateIncludeList(List<Ring<T>> include_list, Query<T> query_obj) {
        if (validate) {
            ArrayList<T> clone = new ArrayList<T>();
            clone.addAll( real_solutions );
            for (Ring<T> ring : include_list) {
                for (T point : ring.getAllContents()) {
                    clone.remove(point);
                }
            }
            if (!clone.isEmpty()) {
                System.out.println("validateIncludeList: Omissions in Include list:" + clone.size());
                System.out.println("validateIncludeList: Include list size = " + include_list.size() );
                int records_in_soln = 0;
                for( Ring<T> ring : include_list ) {
                    records_in_soln += ring.getAllContents().size();
                }
                System.out.println("validateIncludeList: Include list solution size = " + records_in_soln );
                for (T point : clone) {
                    //       System.out.println("\t\tOmission include: " + point );
                }
            } else {
            }
        }
    }
}
