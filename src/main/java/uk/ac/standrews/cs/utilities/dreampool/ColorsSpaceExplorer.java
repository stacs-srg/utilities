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
public class ColorsSpaceExplorer {

    private CartesianPointFileReader source_data;
    private MPool<CartesianPoint> dream_pool;

    private CountingWrapper<CartesianPoint> distance;   // counts number of distance calculations made
    private Euc validate_distance;                      // used to check results.

    private ArrayList<CartesianPoint> datums;
    private ArrayList<CartesianPoint> queries;
    private Set<CartesianPoint> reference_objects;

    private long setup_distance_calcs = 0;

    // Configuration parameters

    private boolean perform_validation = false;          // SET perform_validation TO TRUE TO PERFORM CHECKING

    private float[][] radii = new float[][] {
            new float[]{ 0.07119140625F, 0.106787109375F, 0.1601806640625F, 0.24027099609375F, 0.360406494140625F, 0.540609741210938F }, // 6 rings each * 3/2
            new float[]{ 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F }, // linear 6 far out
            new float[]{ 0.6F, 0.7F, 0.8F }, // linear 3 far out
            new float[]{ 0.1F, 0.2F, 0.3F,},  // linear 3 in close
            new float[]{ 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F }, // linear 6 close in
            new float[]{ 0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F }, // linear 8 rings spread
            new float[]{ 0.0125F, 0.025F, 0.05F, 0.1F, 0.25F, 0.5F } // geometric 6
        };



    // From Richard 28-3-18:
    //     split the data 10:90 and use the 10% as queries over the other 90%
    //     use thresholds of 0.052, 0.083, and 0.131 (supposed to fetch mean 0.01, 0.1 and 1% of data respectively but they don’t in my experience(!)
    //     measure mean number of distance calcs per query; good outcomes are 5k, 10k, 20k
    //

    int source_index = 0;  // track which datums we have used already
    int source_size;       // size of the dataset

    public ColorsSpaceExplorer(String filename ) throws Exception {

        System.out.println("Injesting file " + filename );
        source_data = new CartesianPointFileReader(filename,true);
        source_size = source_data.size();
    }

    private CartesianPoint get_next_source_point() {
        return source_data.get(source_index++ );
    }


    private Set<CartesianPoint> createReferenceObjects(int ros) {

        reference_objects = new HashSet<>();

        for (float pos = 0; pos <ros; pos++ ) {

            CartesianPoint p = get_next_source_point();

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
            CartesianPoint p = get_next_source_point();

            datums.add(p);
            dream_pool.add(p);
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

        float[] thresholds = new float[] { 0.052f, 0.083f, 0.131f }; // 0.01, 0.1 and 1%

        for (int i = 0; i < num_queries; i++) {

            CartesianPoint p = get_next_source_point();

            for (float threshold : thresholds ) {

                result.add(new Query(p, dream_pool, threshold, datums, dream_pool.pools, validate_distance, perform_validation));

                pi.progressStep();
            }
        }

        return result;
    }


    public void doQueries(DataSet dataset, Set<Query<CartesianPoint>> queries, int num_ros, int pool_index ) throws Exception {
        int distance_calcs = 0;

        long initial_calcs = CountingWrapper.counter;  // number of calculations after setup.
        long start_calcs = CountingWrapper.counter;   // number of calculations performed at start of each query

        ProgressIndicator pi = new PercentageProgressIndicator( 100 );
        pi.setTotalSteps(queries.size());

        long start_time = System.currentTimeMillis();

        int count = 0;

        for (Query<CartesianPoint> query : queries) {

            Set<CartesianPoint> results = dream_pool.rangeSearch(query.query, query.threshold,query ); // last parameter for debug only.

            query.validate(results);

            pi.progressStep();

            addRow(dataset, Integer.toString(count), Integer.toString(count), query.threshold, num_ros, pool_index, (int)( CountingWrapper.counter - start_calcs ), results.size());

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

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query", "threshold", "ros", "pool_index", "calculations", "num_results"})));
        doExperiment(dataset);

        createFileIfDoesNotExist(path);
        dataset.print(path);
    }

    private void initialise(int dataset_size, int ros, float[] radii) throws Exception {

        source_index = 0;
        validate_distance = new Euc();
        distance = new CountingWrapper<CartesianPoint>( validate_distance );
        reference_objects = createReferenceObjects(ros);
        dream_pool = new MPool<CartesianPoint>( distance, reference_objects, radii );
        add_data(dataset_size);
        setup_distance_calcs = CountingWrapper.counter;
    }

    private void doExperiment(DataSet dataset) throws Exception {

        System.out.println("Initialising...");

        for( int ref_objs = 20; ref_objs < 150 ; ref_objs+= 40 ) {

            for (int radii_index = 0; radii_index < radii.length; radii_index++) {

                int num_datums = ((source_size - ref_objs) * 9) / 10;       // 90% of data after taking out reference objects;
                int num_queries = source_size - num_datums - ref_objs;      // 10% of the data after taking out reference objects (-1 since index starts at zero)

                initialise(num_datums, ref_objs, radii[radii_index]);
                System.out.println("Generating queries...");
                Set<Query<CartesianPoint>> queries = generateQueries(num_queries);
                System.out.println("Performing queries...");
                doQueries(dataset, queries, ref_objs, radii_index);
            }
        }

    }


    private void addRow(DataSet data, String query, String query2, float threshold,  int count_ros, int pool_index, int count_calcs, int num_results) {

        data.addRow(query, query2, Float.toString(threshold),Integer.toString(count_ros), Integer.toString(pool_index), Integer.toString(count_calcs), Integer.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        System.out.println( "Plotting results...");
        long time = System.currentTimeMillis();
        ColorsSpaceExplorer pr = new ColorsSpaceExplorer( "/Users/al/Desktop/colors.txt" );
        pr.plot("colors-space-RESULTS");
        System.out.println( "Dp finished in " + ( System.currentTimeMillis() - time ) );
    }



}
