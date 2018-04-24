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
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.standrews.cs.utilities.FileManipulation.createFileIfDoesNotExist;

/**
 * Outputs a CSV file of ...
 */
public class PlotResults {

    private MPool<Point> dream_pool;
    private CountingWrapper distance;
    private EuclideanDistance validate_distance; // used to check results.
    private ArrayList<Point> datums;
    private List<Point> reference_objects;
    private long setup_distance_calcs;

    //private int num_datums =    100; //1 hundred
    //private int num_datums =    1000; //1 thousand
    private int num_datums =    10000; //10 thousand
    //private int num_datums =    50000; //50 thousand
    //private int num_datums =    100000; //100 thousand
    //private int num_datums =  1000000; // 1 million

    private double[][] radii = new double[][]{

          //  new float[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2 },
            new double[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2,0.3, 0.4 }   // , 0.61 } //, 0.6 }

//            new float[]{ 0.000004125976563, 0.000008251953125, 0.00001650390625, 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2, 0.4, 0.5 },
//            new float[]{ 0.000004125976563, 0.000008251953125, 0.00001650390625, 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2 },
//            new float[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2 },
//            new float[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2, 0.4 },

//            new float[]{0.1, 0.3, 0.6, 0.7, 0.8},           // sparse and wide
//            new float[]{0.4, 0.5, 0.6, 0.7, 0.8},          // few, far out
            //new float[]{0.1, 0.2, 0.3, 0.4, 0.6, 0.7, 0.8},           // even large spread
            //new float[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8},           // even v.large spread
            // new float[]{0.00625, 0.0125, 0.025, 0.4, 0.5, 0.6, 0.7, 0.8}, // few in close and then a few far out
//            new float[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2 },
//            new float[]{ 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2 },
//            new float[]{ 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2 },
//            new float[]{ 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2, 0.4 },
//            new float[]{ 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2, 0.5 },

//            new float[]{0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2 },
//            new float[]{0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45 },
//            new float[]{0.0125, 0.025, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.5 }, //not any after ).45
            //new float[]{0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.6, 0.7, 0.8} // big spread
    }; // a set of different pool configs to try.



    public PlotResults() {
    }


    private static List<Point> createReferenceObjects(int ros) {

        Random r = new Random(583119234L);  // always use same rand to create reference objects

        List<Point> pts = new ArrayList<>();

        for (float pos = 0; pos <ros; pos++ ) {

            pts.add( newpoint(r) );

        }

        return pts;
    }

    public void add_data(int count) throws Exception {

        Random r = new Random(787819234L);  // always use same rand to create datums
        datums = new ArrayList<>();

        ProgressIndicator pi = new PercentageProgressIndicator( 10 );
        pi.setTotalSteps(count);

        for (int pos = 0; pos < count ; pos++ ) {
            Point p = newpoint(r);
            datums.add( p );
            dream_pool.add(p);
            pi.progressStep();
        }
        dream_pool.completeInitialisation();
    }

    public Set<Query<Point>> generateQueries(int num_queries, boolean perform_validation) {

        HashSet<Query<Point>> result = new HashSet<>();

        Random r = new Random(1926373034L); // do same queries each call of doQueries.

        ProgressIndicator pi = new PercentageProgressIndicator( 10 );
        pi.setTotalSteps(num_queries);

        for (int i = 0; i < num_queries; i++) {

            for (float range = 2.0F; range <= 32.0F; range *= 4.0F) {     // gives different threshold ranges 0..1/2,1/8,1/32 x,y plane
                float threshold = r.nextFloat() / range;

                Point p = newpoint(r);
                result.add(new Query(p, dream_pool, threshold, datums, dream_pool.pools, validate_distance, perform_validation));
                pi.progressStep();
            }
        }

        return result;
    }


    public void doQueries(DataSet dataset, Set<Query<Point>> queries, int num_ros, int pool_index, boolean parallel) throws Exception {

        ProgressIndicator pi = new PercentageProgressIndicator( 100 );
        pi.setTotalSteps(queries.size());

        int distance_calcs = 0;
        long initial_calcs = distance.counter;

        long start_time = System.currentTimeMillis();

        for (Query<Point> query : queries) {

            distance.reset();

            List<Point> results;

            results = dream_pool.rangeSearch(query.query, query.threshold, query); // last parameter for debug only.

            query.validate(results);

            pi.progressStep();

            addRow(dataset, query.query.x, query.query.y, query.threshold, num_ros, pool_index, (int)(distance.counter), results.size());

            distance_calcs += distance.counter;

        }
        long elapsed_time = System.currentTimeMillis() - start_time;

        System.out.println( "Queries performed: " + queries.size() + " datums = " + datums.size() + " ros = " + num_ros + " total distance calcs = " + ( initial_calcs + distance_calcs ) + " distance calcs during queries = " + ( distance_calcs ) + " in " + elapsed_time + "ms qps = " + ( queries.size() * 1000 ) / elapsed_time + " q/s" );
    }

    /************** Private **************/

    private void plot(String fname, boolean in_parallel, boolean validate) throws Exception {

        String results_path = "/Users/al/Desktop/" + fname + ".csv";

        Path path = Paths.get(results_path);

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query_x", "query_y", "threshold", "ros", "pool_index", "calculations", "num_results"})));
        doExperiment(dataset, in_parallel, validate );
        // oneExperiment(dataset);

        createFileIfDoesNotExist(path);
        dataset.print(path);
    }

    private void initialise(int dataset_size, int ros, double[] radii) throws Exception {

        validate_distance = new EuclideanDistance();
        distance = new CountingWrapper( validate_distance );
        reference_objects = createReferenceObjects(ros);
        dream_pool = new MPool<Point>( distance, reference_objects, radii );
        add_data(dataset_size);
        setup_distance_calcs = distance.counter;
    }

    private void doExperiment(DataSet dataset, boolean parallel, boolean validate) throws Exception {

        // for( int ref_objs = 60; ref_objs < 65 ; ref_objs+= 1 ) {

        int ref_objs = 62;
        int radii_index = 0;

            // for(int radii_index = 0; radii_index < radii.length; radii_index++ ) {


                System.out.println( "Initialising..." );
                initialise(num_datums, ref_objs, radii[radii_index]);
                System.out.println( "Generating queries..." );
                Set<Query<Point>> queries = generateQueries(100, validate);
                System.out.println( "Performing queries..." );
                doQueries( dataset, queries, ref_objs, radii_index, parallel );
        //    }
        // }
    }

    private static Point newpoint(Random r) {

        float x;
        float y;

        do {
            x = r.nextFloat();
            y = r.nextFloat();
        } while( Math.sqrt( Math.pow( 0.5 - x, 2 ) + Math.pow( 0.5 - y, 2 ) ) > 0.5f ); // all points 0.5 away from 0.5,0.5 = unit circle

        return new Point( x, y );
    }

    private void addRow(DataSet data, float queryx, float queryy, float threshold,  int count_ros, int radii_index, int count_calcs, int num_results) {

        data.addRow(Float.toString(queryx), Float.toString(queryy), Float.toString(threshold),Integer.toString(count_ros), Integer.toString(radii_index), Integer.toString(count_calcs), Integer.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        System.out.println( "Plotting results...");
        long time = System.currentTimeMillis();
        PlotResults pr = new PlotResults();
        pr.plot("RESULTS",false,false);
        System.out.println( "Dp finished in " + ( System.currentTimeMillis() - time ) );
    }



}
