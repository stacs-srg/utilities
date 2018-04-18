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

    // Configuration parameters

    private boolean perform_validation = true;          // SET perform_validation TO TRUE TO PERFORM CHECKING

    //private int num_datums =    100; //1 hundred
    //private int num_datums =    1000; //1 thousand
    private int num_datums =    10000; //10 thousand
    //private int num_datums =    50000; //50 thousand
    //private int num_datums =    100000; //100 thousand
    //private int num_datums =  1000000; // 1 million

    private float[][] radii = new float[][]{

          //  new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F },
            new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F,0.3F, 0.4F }   // , 0.61F } //, 0.6F }

//            new float[]{ 0.000004125976563F, 0.000008251953125F, 0.00001650390625F, 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F, 0.4F, 0.5F },
//            new float[]{ 0.000004125976563F, 0.000008251953125F, 0.00001650390625F, 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F },
//            new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F },
//            new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F, 0.4F },

//            new float[]{0.1F, 0.3F, 0.6F, 0.7F, 0.8F},           // sparse and wide
//            new float[]{0.4F, 0.5F, 0.6F, 0.7F, 0.8F},          // few, far out
            //new float[]{0.1F, 0.2F, 0.3F, 0.4F, 0.6F, 0.7F, 0.8F},           // even large spread
            //new float[]{0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F},           // even v.large spread
            // new float[]{0.00625F, 0.0125F, 0.025F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F}, // few in close and then a few far out
//            new float[]{ 0.0000330078125F, 0.000066015625F, 0.00013203125F, 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F },
//            new float[]{ 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F },
//            new float[]{ 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F },
//            new float[]{ 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F, 0.4F },
//            new float[]{ 0.0002640625F, 0.000528125F, 0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.2F, 0.5F },

//            new float[]{0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F },
//            new float[]{0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.45F },
//            new float[]{0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.5F }, //not any after ).45
            //new float[]{0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.45F, 0.6F, 0.7F, 0.8F} // big spread
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

    public Set<Query<Point>> generateQueries(int num_queries) {

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


    public void doQueries(DataSet dataset, Set<Query<Point>> queries, int num_ros, int pool_index) throws Exception {
        int distance_calcs = 0;

        long initial_calcs = CountingWrapper.counter;  // number of calculations after setup.
        long start_calcs = CountingWrapper.counter;   // number of calculations performed at start of each query

        ProgressIndicator pi = new PercentageProgressIndicator( 100 );
        pi.setTotalSteps(queries.size());

        long start_time = System.currentTimeMillis();

        for (Query<Point> query : queries) {

            Set<Point> results = dream_pool.rangeSearch(query.query, query.threshold,query ); // last parameter for debug only.

            query.validate(results);

            pi.progressStep();

            addRow(dataset, query.query.x, query.query.y, query.threshold, num_ros, pool_index, (int)(CountingWrapper.counter - start_calcs), results.size());

            start_calcs = CountingWrapper.counter;

        }
        long elapsed_time = System.currentTimeMillis() - start_time;

        System.out.println( "Queries performed: " + queries.size() + " datums = " + datums.size() + " ros = " + num_ros + " total distance calcs = " + CountingWrapper.counter + " distance calcs during queries = " + ( CountingWrapper.counter - initial_calcs ) + " in " + elapsed_time + "ms qps = " + ( queries.size() * 1000 ) / elapsed_time + " q/s" );
    }

    /************** Private **************/

    private void plot(String fname) throws Exception {

        String results_path = "/Users/al/Desktop/" + fname + ".csv";

        Path path = Paths.get(results_path);

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query_x", "query_y", "threshold", "ros", "pool_index", "calculations", "num_results"})));
        doExperiment(dataset);
        // oneExperiment(dataset);

        createFileIfDoesNotExist(path);
        dataset.print(path);
    }

    private void initialise(int dataset_size, int ros, float[] radii) throws Exception {

        validate_distance = new EuclideanDistance();
        distance = new CountingWrapper( validate_distance );
        reference_objects = createReferenceObjects(ros);
        dream_pool = new MPool<Point>( distance, reference_objects, radii );
        add_data(dataset_size);
        setup_distance_calcs = CountingWrapper.counter;
    }

    private void doExperiment(DataSet dataset) throws Exception {

        // for( int ref_objs = 60; ref_objs < 65 ; ref_objs+= 1 ) {

        int ref_objs = 62;
        int radii_index = 0;

            // for(int radii_index = 0; radii_index < radii.length; radii_index++ ) {


                System.out.println( "Initialising..." );
                initialise(num_datums, ref_objs, radii[radii_index]);
                System.out.println( "Generating queries..." );
                Set<Query<Point>> queries = generateQueries(100);
                System.out.println( "Performing queries..." );
                doQueries(dataset, queries, ref_objs, radii_index);
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
        pr.plot("RESULTS");
        System.out.println( "Dp finished in " + ( System.currentTimeMillis() - time ) );
    }



}
