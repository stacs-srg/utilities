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

import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.LoggingLevel;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.dreampool.richard.dataPoints.cartesian.Euclidean;
import uk.ac.standrews.cs.utilities.dreampool.richard.testloads.TestContext;
import uk.ac.standrews.cs.utilities.metrics.CartesianPoint;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.standrews.cs.utilities.FileManipulation.createFileIfDoesNotExist;
import static uk.ac.standrews.cs.utilities.Logging.output;

/**
 * Outputs a CSV file of ...
 */
public class ColorsFull {


    private MPool<CartesianPoint> dream_pool;

    private CountingWrapper<CartesianPoint> distance;   // counts number of distance calculations made
    private Euclidean validate_distance;                      // used to check results.

    private List<CartesianPoint> data;
    private List<CartesianPoint> queries;
    private List<CartesianPoint> pivots;

    private long setup_distance_calcs = 0;

    // Configuration parameters

    private static boolean perform_validation = false;  // set perform_validation to perform validation.
    private static boolean brute_force = false;         // do brute force rather than MPool.
    private static boolean check_results = false;       // whether to check results of queries for correctness (less exhaustive than full evaluation).
    private static boolean exploring = false;           // set to perform space exploration (radii and ros).

    private float[][] exploration_radii = new float[][] {
            new float[]{ 0.07119140625F, 0.106787109375F, 0.1601806640625F, 0.24027099609375F, 0.360406494140625F, 0.540609741210938F }, // 6 rings each * 3/2
            new float[]{ 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F }, // linear 6 far out
            new float[]{ 0.6F, 0.7F, 0.8F }, // linear 3 far out
            new float[]{ 0.1F, 0.2F, 0.3F,},  // linear 3 in close
            new float[]{ 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F }, // linear 6 close in
            new float[]{ 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F }, // linear 8 rings spread
            new float[]{ 0.0125F, 0.025F, 0.05F, 0.1F, 0.25F, 0.5F } // geometric 6
    };

    private float[] radii = new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F,0.3F, 0.4F }; // these work well with Euclidean


    // From Richard 28-3-18:
    //     split the data 10:90 and use the 10% as queries over the other 90%
    //     use thresholds of 0.052, 0.083, and 0.131 (supposed to fetch mean 0.01, 0.1 and 1% of data respectively but they don’t in my experience(!)
    //     measure mean number of distance calcs per query; good outcomes are 5k, 10k, 20k
    //

    int source_index = 0;  // track which datums we have used already
    int source_size;       // size of the dataset

    public ColorsFull( String filename, int num_pivots ) throws Exception {

        output( LoggingLevel.SHORT_SUMMARY, "perform validation = " + perform_validation );
        output( LoggingLevel.SHORT_SUMMARY, "brute force = " + brute_force );
        output( LoggingLevel.SHORT_SUMMARY, "check results = " + check_results );
        output( LoggingLevel.SHORT_SUMMARY, "exploring = " + exploring );
        output( LoggingLevel.SHORT_SUMMARY, "Injesting file " + filename );

        TestContext tc = new TestContext(TestContext.Context.colors);
        tc.setSizes(tc.dataSize() / 10, num_pivots);

        data =tc.getData();
        pivots = tc.getRefPoints();
        queries = tc.getQueries();

        source_size = data.size();
    }


    public void addData() throws Exception {

        for( CartesianPoint datum :data ) {

            dream_pool.add(datum);
        }
    }

    public Set<Query<CartesianPoint>> generateQueries(int num_queries) {

        HashSet<Query<CartesianPoint>> result = new HashSet<>(); // Could fold these but can't be bothered!

        Random r = new Random(1926373034L); // do same queries each call of doQueries.

        float[] thresholds = new float[] { 0.052f, 0.083f, 0.131f }; // 0.01, 0.1 and 1%

        for (CartesianPoint p : queries ) {

                for (float threshold : thresholds ) {

                    result.add(new Query(p, dream_pool, threshold, data, dream_pool.pools, validate_distance, perform_validation));
                }
        }

        return result;
    }


    public void doQueries(DataSet dataset, Set<Query<CartesianPoint>> queries, int num_ros, int pool_index ) throws Exception {
        int distance_calcs = 0;

        long initial_calcs = CountingWrapper.counter;  // number of calculations after setup.
        long start_calcs = CountingWrapper.counter;   // number of calculations performed at start of each query

        long start_time = System.currentTimeMillis();

        int count = 0;

        for (Query<CartesianPoint> query : queries) {

            Set<CartesianPoint> results;

            if( brute_force ) {
                results = bruteForce( query.query, query.threshold,query );
            } else {
                results = dream_pool.rangeSearch(query.query, query.threshold,query ); // last parameter for debug only.
            }
            query.validate(results);
            if( check_results ) {
                query.checkSolutions(results);
            }

            addRow(dataset, Integer.toString(count), query.threshold, num_ros, pool_index, CountingWrapper.counter - start_calcs, query.getHPExclusions(), query.getPivotInclusions(), query.getPivotExclusions(), query.getRequiringFiltering(), results.size());

            count++;

            start_calcs = CountingWrapper.counter;

        }
        long elapsed_time = System.currentTimeMillis() - start_time;

        output( LoggingLevel.SHORT_SUMMARY, "Queries performed: " + queries.size() + " datums = " + data.size() + " ros = " + num_ros + " total distance calcs = " + CountingWrapper.counter + " distance calcs during queries = " + ( CountingWrapper.counter - initial_calcs ) + " in " + elapsed_time / 1000 + "s qps = " + ( queries.size() * 1000 ) / elapsed_time + " q/s" );
    }

    private Set<CartesianPoint> bruteForce(CartesianPoint query, float threshold, Query<CartesianPoint> query1) {

        Set<CartesianPoint> result = new HashSet<>();

        for( CartesianPoint datum : data ) {

            float d = validate_distance.distance(datum, query);

            if (d <= threshold) {
                result.add(datum);
            }
        }
        return result;
    }

    public void plot(String fname) throws Exception {

        String results_path = "/Users/al/Desktop/" + fname + ".csv";

        Path path = Paths.get(results_path);

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query", "threshold", "ros", "pool index", "calculations", "hp exlusions", "pivot inclusions", "pivot exclusions", "requiring filtering", "num_results"})));
        if( exploring ) {
            explore( dataset );
        } else {
            doExperiment(dataset);
        }

        createFileIfDoesNotExist(path);
        dataset.print(path);
    }

    private void initialise(float[] radii) throws Exception {

        validate_distance = new Euclidean();
        distance = new CountingWrapper<CartesianPoint>( validate_distance );
        dream_pool = new MPool<CartesianPoint>( distance, pivots, radii );
        addData();
        dream_pool.completeInitialisation();
        setup_distance_calcs = CountingWrapper.counter;
    }

    private void explore(DataSet dataset) throws Exception {

        System.out.println("Initialising exploration...");

        int num_queries = queries.size();        // 10% of the data after taking out reference objects

        for( int num_ref_objs = 20; num_ref_objs < 150 ; num_ref_objs+= 40 ) {

            for (int radii_index = 0; radii_index < radii.length; radii_index++) {

                initialise(exploration_radii[radii_index]);
                System.out.println("Generating queries...");
                Set<Query<CartesianPoint>> generated_queries = generateQueries(num_queries);
                System.out.println("Performing queries...");
                doQueries(dataset, generated_queries, num_ref_objs, radii_index);
            }
        }

    }

    private void doExperiment(DataSet dataset) throws Exception {

        output( LoggingLevel.SHORT_SUMMARY, "Initialising experiment...");

        int num_datums = data.size();            // 90% of data after taking out reference objects;
        int num_queries = queries.size();        // 10% of the data after taking out reference objects
        int num_ref_objs = pivots.size();

        output( LoggingLevel.SHORT_SUMMARY, "source_size = " + source_size );
        output( LoggingLevel.SHORT_SUMMARY, "ref objects = " + num_ref_objs );
        output( LoggingLevel.SHORT_SUMMARY, "datums = " + num_datums );
        output( LoggingLevel.SHORT_SUMMARY, "queries = " + num_queries );

        initialise(radii);
        output( LoggingLevel.SHORT_SUMMARY, "Generating queries...");
        Set<Query<CartesianPoint>> generated_queries = generateQueries(num_queries);
        output( LoggingLevel.SHORT_SUMMARY, "Performing queries...");
        doQueries(dataset, generated_queries, num_ref_objs, 0);

    }


    private void addRow(DataSet data, String query, float threshold,  long count_ros, long pool_index, long count_calcs, int hp_exlude, int pivot_include, int pivot_exclude, int requiring_filtering, long num_results) {

        data.addRow(query, Float.toString(threshold),Long.toString(count_ros), Long.toString(pool_index), Long.toString(count_calcs), Integer.toString(hp_exlude), Integer.toString(pivot_include), Integer.toString(pivot_exclude),Integer.toString(requiring_filtering), Long.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        int num_ref_objs = 25;
        check_results = true;
        perform_validation = false;
        brute_force = false;
        exploring = false;

        Logging.setLoggingLevel(LoggingLevel.VERBOSE);
        output( LoggingLevel.SHORT_SUMMARY, "Plotting results...");
        long time = System.currentTimeMillis();
        ColorsFull pr = new ColorsFull( "colors", num_ref_objs );
        pr.plot("colors-RESULTS");
        output( LoggingLevel.SHORT_SUMMARY, "Dp finished in " + ( System.currentTimeMillis() - time ) / 1000 + "s" );
    }



}
