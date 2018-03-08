/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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
    private Set<Point> reference_objects;
    private int setup_distance_calcs;
    private float[][] radii = new float[][]{
//            new float[]{0.1F, 0.3F, 0.6F, 0.7F, 0.8F},           // sparse and wide
//            new float[]{0.4F, 0.5F, 0.6F, 0.7F, 0.8F},          // few, far out
            new float[]{0.1F, 0.2F, 0.3F, 0.4F, 0.6F, 0.7F, 0.8F},           // even large spread
            new float[]{0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F},           // even v.large spread
            new float[]{0.00625F, 0.0125F, 0.025F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F}, // few in close and then a few far out
            new float[]{0.00105625F, .003125F, 0.00625F, 0.0125F, 0.025F, 0.05F, 0.1F, 0.15F, 0.2F, 0.25F, 0.3F, 0.35F, 0.4F, 0.45F, 0.6F, 0.7F, 0.8F} // big spread
    }; // a set of different pool configs to try.



    public PlotResults() {
    }


    private static Set<Point> createReferenceObjects(int ros) {

        Random r = new Random(583119234L);  // always use same rand to create reference objects

        Set<Point> pts = new HashSet<>();

        for (float pos = 0; pos <ros; pos++ ) {

            pts.add( newpoint(r) );

        }
        // provided that circles are sufficiently large
        // unit circles cover square of side 2√2
        // will get total coverage of unit square (proved by Denes Nagy) http://www2.stetson.edu/~efriedma/circovsqu/
        // This is a cheat but will eliminate lack of coverage as a problem.
        // TODO revisit this.
//        pts.add( new Point(0.25F,0.25F ) );
//        pts.add( new Point(0.25F,0.75F ) );
//        pts.add( new Point(0.75F,0.25F ) );
//        pts.add( new Point(0.75F,0.75F ) );
        return pts;
    }

    public void add_data(int count) throws Exception {

        Random r = new Random(787819234L);  // always use same rand to create datums
        datums = new ArrayList<>();

        for (float pos = 0; pos < count ; pos++ ) {
            Point p = newpoint(r);
            datums.add( p );
            dream_pool.add(p);
        }
    }

    public Set<Query<Point>> generateQueries(int num_queries) {

        HashSet<Query<Point>> result = new HashSet<>();

        Random r = new Random(1926373034L); // do same doQueries each call of doQueries.

        for (int i = 0; i < num_queries; i++) {
            for (float range = 2.0F; range <= 32.0F; range *= 4.0F) {     // gives different threshold ranges 0..1/2,1/8,1/32 x,y plane
                float threshold = r.nextFloat() / range;
                result.add(new Query(newpoint(r), threshold, datums, dream_pool.pools, validate_distance));
            }
        }

        return result;
    }


    public void doQueries(DataSet dataset, Set<Query<Point>> queries, int num_ros, int pool_index) throws Exception {
        int distance_calcs = 0;

        int start_calcs = CountingWrapper.counter;
        int total_calcs = 0;

        for (Query<Point> query : queries) {

            // Set<Point> results = dream_pool.rangeSearchWithHyperplane(query.query, query.threshold, query ); // last parameter for debug only.
            Set<Point> results = dream_pool.rangeSearch(query.query, query.threshold ); // last parameter for debug only.

            query.validate(results);

            distance_calcs = CountingWrapper.counter - start_calcs;
            total_calcs += distance_calcs;
            start_calcs = CountingWrapper.counter;

            addRow(dataset, query.query.x, query.query.y, query.threshold, num_ros, pool_index, distance_calcs, results.size());
        }
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

        int num_datums = 10000;

        for( int ref_objs = 80; ref_objs < 200 ; ref_objs+= 10 ) {
            for(int radii_index = 0; radii_index < radii.length; radii_index++ ) {
                initialise(num_datums, ref_objs, radii[radii_index]);
                Set<Query<Point>> queries = generateQueries(20);
                doQueries(dataset, queries, ref_objs, radii_index);
            }
        }
    }

    private void oneExperiment(DataSet dataset) throws Exception {

        int num_datums = 10000;

        int ref_objs = 30;
        int radii_index = 1;

        initialise(num_datums, ref_objs, radii[radii_index]);

        Query<Point> q = new Query<>( new Point( 0.74204534F,0.058961034F ),0.21586731F, datums, dream_pool.pools, validate_distance );

        Set<Point> results = dream_pool.rangeSearchWithHyperplane(q.query, q.threshold, q ); // last parameter for debug only.

        q.validate(results);
    }


    private static Point newpoint(Random r) {

        return new Point( r.nextFloat(), r.nextFloat() );
    }

    private void addRow(DataSet data, float queryx, float queryy, float threshold,  int count_ros, int radii_index, int count_calcs, int num_results) {

        data.addRow(Float.toString(queryx), Float.toString(queryy), Float.toString(threshold),Integer.toString(count_ros), Integer.toString(radii_index), Integer.toString(count_calcs), Integer.toString(num_results));
    }

    public static void main(String[] args) throws Exception {

        System.out.println( "Plotting results...");
        PlotResults pr = new PlotResults();
        pr.plot("RESULTS");
        System.out.println( "finished");
    }



}
