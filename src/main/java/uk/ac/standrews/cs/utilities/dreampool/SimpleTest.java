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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SimpleTest {

    private static MPool<Point> dream_pool;
    private static CountingWrapper distance;
    private static Random r = new Random(787819234L);
    private static ArrayList<Point> points = new ArrayList<>();
    private static long setup_distance_calcs;
    private static Set<Point> ros;

    @BeforeClass
    public static void setUp() throws Exception {

        distance = new CountingWrapper( new EuclideanDistance() );
        ros = createReferenceObjects();
        dream_pool = new MPool<Point>( distance, ros );
//        System.out.println( "Distance calculations (during ro initialisation): " + CountingWrapper.counter );
        add_data();
        setup_distance_calcs = CountingWrapper.counter;
//        System.out.println( "Distance calculations (after adding data): " + CountingWrapper.counter );
    }

    private static Set<Point> createReferenceObjects() {
        Set<Point> pts = new HashSet<>();

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
        Query q = new Query( query, dream_pool, threshold, points, dream_pool.pools, distance, true );
        System.out.println("Query: " + query + " Threshold: " + threshold);
        Set<Point> results = dream_pool.rangeSearch(query, threshold, q );
        System.out.println("Total Distance calculations: " + CountingWrapper.counter);
        System.out.println("Distance calculations (query only): " + (CountingWrapper.counter - setup_distance_calcs));
    }

    private static Point newpoint() {

        return new Point( r.nextFloat(), r.nextFloat() );
    }

    private void show() {
        dream_pool.show_structure();
    }


}
