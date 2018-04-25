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

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleTest {

    private static MPool<Point> dream_pool;
    private static CountingWrapper distance;
    private static Random r = new Random(787819234L);
    private static ArrayList<Point> points = new ArrayList<>();
    private static long setup_distance_calcs;
    private static List<Point> ros;

    @BeforeClass
    public static void setUp() throws Exception {

        distance = new CountingWrapper( new EuclideanDistance() );
        ros = createReferenceObjects();
        double[] radii = new double[]{ 0.0000330078125, 0.000066015625, 0.00013203125, 0.0002640625, 0.000528125, 0.00105625, .003125, 0.00625, 0.0125, 0.025, 0.05, 0.1, 0.2,0.3, 0.4 };

        dream_pool = new MPool<Point>( distance, ros,radii );
//        System.out.println( "Distance calculations (during ro initialisation): " + CountingWrapper.counter );
        add_data();
        setup_distance_calcs = distance.counter;
//        System.out.println( "Distance calculations (after adding data): " + CountingWrapper.counter );
    }

    private static List<Point> createReferenceObjects() {
        List<Point> pts = new ArrayList<>();

        for (float pos = 0; pos <30; pos++ ) {

            pts.add( newpoint() );

        }
        return pts;
    }

    public static void add_data() throws Exception {
        for (int pos = 0; pos < 10000 ; pos++ ) {
            Point p = newpoint();
            points.add( p );
            dream_pool.add(p);
        }
        dream_pool.completeInitialisation();
    }


    @Test
    public void queryOnce() throws Exception {
        Point query = new Point(0.5f, 0.5f);
        float threshold = 0.05f;
        distance.reset();
        Query q = new Query( query, dream_pool, threshold, points, dream_pool.pools, distance, true );
        System.out.println("Query: " + query + " Threshold: " + threshold);
        List<Point> results = dream_pool.diagnosticRangeSearch(query, threshold, q );
        System.out.println("Total Distance calculations: " + distance.counter);
        System.out.println("Distance calculations (query only): " + (distance.counter - setup_distance_calcs));
    }

    private static Point newpoint() {

        return new Point( r.nextFloat(), r.nextFloat() );
    }

}
