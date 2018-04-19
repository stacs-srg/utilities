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
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static uk.ac.standrews.cs.utilities.FileManipulation.createFileIfDoesNotExist;
import static uk.ac.standrews.cs.utilities.Logging.output;

/**
 * Outputs a CSV file of ...
 */
public class EuclidianResults {

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



    public EuclidianResults() {
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

        for (int pos = 0; pos < count ; pos++ ) {
            Point p = newpoint(r);
            datums.add( p );
            dream_pool.add(p);
        }
        dream_pool.completeInitialisation();
    }


    public void doQueries(DataSet dataset, int num_queries, int num_ros ){

        Random r = new Random(1926373034L); // do same queries each call of doQueries.

        long start_time = System.currentTimeMillis();
        long start_calcs = CountingWrapper.counter;   // number of calculations performed at start of each query

        int count = 0;

        for (int i = 0; i < num_queries; i++) {

            for (float range = 2.0F; range <= 32.0F; range *= 4.0F) {     // gives different threshold ranges 0..1/2,1/8,1/32 x,y plane
                float threshold = r.nextFloat() / range;

                Point p = newpoint(r);
                Query query = new Query(p, dream_pool, threshold, datums, dream_pool.pools, validate_distance, perform_validation);

                Set<Point> results = dream_pool.rangeSearch(p, threshold, query); // last parameter for debug only.
                query.validate( results );

                addRow(dataset, p.x, p.y, query.threshold, num_ros, (int)(CountingWrapper.counter - start_calcs),
                        query.getHPExclusions(),query.getPivotInclusions(),query.getPivotExclusions(),query.getRequiringFiltering(), results.size());

                count++;
            }

        }
        long elapsed_time = System.currentTimeMillis() - start_time;

        output( LoggingLevel.SHORT_SUMMARY, "Queries performed: " + count + " datums = " + datums.size() + " qps = " + ( count * 1000 ) / elapsed_time + " q/s average query time = " + elapsed_time / count + " ms"  );
    }

    /************** Private **************/

    private void plot(String fname) throws Exception {

        String results_path = "/Users/al/Desktop/" + fname + ".csv";

        Path path = Paths.get(results_path);

        DataSet dataset = new DataSet(new ArrayList<>(Arrays.asList(new String[]{"query x", "query y", "threshold", "ros", "calculations", "hp exlusions", "pivot inclusions", "pivot exclusions", "requiring filtering", "num_results"})));
        doExperiment(dataset);

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

    private void doExperiment( DataSet dataset ) throws Exception {

        int ref_objs = 100;
        int radii_index = 0;

        output( LoggingLevel.VERBOSE, "Initialising...");
        initialise(num_datums, ref_objs, radii[radii_index]);
        output( LoggingLevel.VERBOSE, "Performing queries...");
        doQueries( dataset, 100, ref_objs);
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

    private void addRow(DataSet data, float queryx, float queryy, float threshold, int count_ros, int count_calcs, int hp_exlude, int pivot_include, int pivot_exclude, int requiring_filtering, int num_results) {

        data.addRow(Float.toString(queryx), Float.toString(queryy), Float.toString(threshold),Integer.toString(count_ros), Integer.toString(count_calcs), Integer.toString(hp_exlude), Integer.toString(pivot_include), Integer.toString(pivot_exclude),Integer.toString(requiring_filtering),Integer.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        Logging.setLoggingLevel(LoggingLevel.VERBOSE);
        long time = System.currentTimeMillis();
        EuclidianResults er = new EuclidianResults();
        er.plot("Euclidean-RESULTS");
        output( LoggingLevel.SHORT_SUMMARY, "Dp finished in " + ( System.currentTimeMillis() - time ) / 1000 + "s" );
    }



}
