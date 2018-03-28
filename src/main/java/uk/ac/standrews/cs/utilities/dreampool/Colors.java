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

import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.CartesianPoint;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.standrews.cs.utilities.FileManipulation.createFileIfDoesNotExist;

/**
 * Outputs a CSV file of ...
 */
public class Colors {

    private final CartesianPointFileReader cpfr;
    private MPool<CartesianPoint> dream_pool;

    private CountingWrapper<CartesianPoint> distance;   // counts number of distance calculations made
    private Euc validate_distance;                      // used to check results.

    private ArrayList<CartesianPoint> datums;
    private ArrayList<CartesianPoint> queries;
    private Set<CartesianPoint> reference_objects;

    private int setup_distance_calcs = 0;

    // Configuration parameters

    private boolean perform_validation = false;          // SET perform_validation TO TRUE TO PERFORM CHECKING

    private float[] radii = new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F,0.3F, 0.4F };

    //private int num_datums =    100; //1 hundred
    //private int num_datums =    1000; //1 thousand
    private int num_datums =    10000; //10 thousand
    //private int num_datums =    50000; //50 thousand
    //private int num_datums =    100000; //100 thousand

    // 112,000 entries in file.


    public Colors(String filename ) throws Exception {

        cpfr = new CartesianPointFileReader(filename,true);

    }


    private Set<CartesianPoint> createReferenceObjects(int ros) {

        reference_objects = new HashSet<>();

        for (float pos = 0; pos <ros; pos++ ) {

            CartesianPoint p = cpfr.randomValue();
            while( reference_objects.contains(p) ) {
                p = cpfr.randomValue();
            }

            reference_objects.add( p );

        }

        return reference_objects;
    }

    public void add_data(int count) throws Exception {

        Random r = new Random(787819234L);  // always use same rand to create datums
        datums = new ArrayList<>();

        ProgressIndicator pi = new PercentageProgressIndicator( 10 );
        pi.setTotalSteps(count);

        for( int pos = 0; pos < count; pos++ ) {
            CartesianPoint p= cpfr.randomValue();
            while( datums.contains(p)) {
                p = cpfr.randomValue();
            }
            datums.add(p);
            dream_pool.add(p,pos);
            pi.progressStep();
        }

        dream_pool.completeInitialisation();
    }

    public Set<Query<CartesianPoint>> generateQueries(int num_queries) {

        HashSet<Query<CartesianPoint>> result = new HashSet<>(); // Could fold these but can't be bothered!
        queries = new ArrayList<>();

        Random r = new Random(1926373034L); // do same queries each call of doQueries.

        ProgressIndicator pi = new PercentageProgressIndicator( 10 );
        pi.setTotalSteps(num_queries);

        for (int i = 0; i < num_queries; i++) {

            for (float range = 2.0F; range <= 32.0F; range *= 4.0F) {     // gives different threshold ranges 0..1/2,1/8,1/32 x,y plane
                float threshold = r.nextFloat() / range;

                CartesianPoint p = cpfr.randomValue();
                while( queries.contains(p)) {
                    p = cpfr.randomValue();
                }

                result.add(new Query(p, threshold, datums, dream_pool.pools, validate_distance, perform_validation));
                pi.progressStep();
            }
        }

        return result;
    }


    public void doQueries(DataSet dataset, Set<Query<CartesianPoint>> queries, int num_ros ) throws Exception {
        int distance_calcs = 0;

        int initial_calcs = CountingWrapper.counter;  // number of calculations after setup.
        int start_calcs = CountingWrapper.counter;   // number of calculations performed at start of each query

        ProgressIndicator pi = new PercentageProgressIndicator( 100 );
        pi.setTotalSteps(queries.size());

        long start_time = System.currentTimeMillis();

        int count = 0;

        for (Query<CartesianPoint> query : queries) {

            Set<CartesianPoint> results = dream_pool.rangeSearch(query.query, query.threshold,query ); // last parameter for debug only.

            query.validate(results);

            pi.progressStep();

            addRow(dataset, Integer.toString(count), query.threshold, num_ros, CountingWrapper.counter - start_calcs, results.size());

            count++;

            start_calcs = CountingWrapper.counter;

        }
        long elapsed_time = System.currentTimeMillis() - start_time;

        System.out.println( "Queries performed: " + queries.size() + " datums = " + datums.size() + " ros = " + num_ros + " total distance calcs = " + CountingWrapper.counter + " distance calcs during queries = " + ( CountingWrapper.counter - initial_calcs ) + " in " + elapsed_time + "ms qps = " + ( queries.size() * 1000 ) / elapsed_time + " q/s" );
    }

    /************** Private **************/

    private void plot(String fname) throws Exception {

        String results_path = "/Users/al/Desktop/" + fname + ".csv";

        Path path = Paths.get(results_path);

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query", "threshold", "ros", "calculations", "num_results"})));
        doExperiment(dataset);
        // oneExperiment(dataset);

        createFileIfDoesNotExist(path);
        dataset.print(path);
    }

    private void initialise(int dataset_size, int ros, float[] radii) throws Exception {

        validate_distance = new Euc();
        distance = new CountingWrapper<CartesianPoint>( validate_distance );
        reference_objects = createReferenceObjects(ros);
        dream_pool = new MPool<CartesianPoint>( distance, reference_objects, radii );
        add_data(dataset_size);
        setup_distance_calcs = CountingWrapper.counter;
    }

    private void doExperiment(DataSet dataset) throws Exception {


        int ref_objs = 62;

        System.out.println("Initialising...");
        initialise(num_datums, ref_objs, radii);
        System.out.println("Generating queries...");
        Set<Query<CartesianPoint>> queries = generateQueries(100);
        System.out.println("Performing queries...");
        doQueries(dataset, queries, ref_objs);

    }


    private void addRow(DataSet data, String query, float threshold,  int count_ros, int count_calcs, int num_results) {

        data.addRow(query, Float.toString(threshold),Integer.toString(count_ros), Integer.toString(count_calcs), Integer.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        System.out.println( "Plotting results...");
        long time = System.currentTimeMillis();
        Colors pr = new Colors( "/Users/al/Desktop/colors.txt" );
        pr.plot("colors-RESULTS");
        System.out.println( "Dp finished in " + ( System.currentTimeMillis() - time ) );
    }



}
