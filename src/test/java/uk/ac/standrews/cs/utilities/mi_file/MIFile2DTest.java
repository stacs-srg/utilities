/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.mi_file;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;


public class MIFile2DTest {

    private MIFile<Point> file;
    private EuclideanDistance distance;

    @Before
    public void setUp() throws Exception {

        distance = new EuclideanDistance();
        Set<Point>  ros = createReferenceObjects();
        file = new MIFile<>(distance,ros,10,10); // TODO investigate these numbers!
    }

    private Set<Point> createReferenceObjects() {
        Set<Point> pts = new HashSet<>();
        for (double step = 10.0F; step < 80.0F; step+=10.0F) {

            pts.add(new Point(+step + 1.0f, +step + 2.0f ));
            pts.add(new Point(-step - 3.0f , -step - 4.0f ));
            pts.add(new Point(-step - 5.0f , +step + 6.0f ));
            pts.add(new Point(+step + 7.0f , -step - 8.0f));
        }
        return pts;
    }

    /**
     * average_value a single point to the file
     */
    @Test
    public void addNone() {

        assertEquals(0, file.size());
        assertFalse(file.contains(new Point(0.0F, 0.0F)));
    }

    /**
     * average_value a single point to the file
     */
    @Test
    public void addOne() throws Exception {

        file.add(new Point(0.0F, 0.0F));

        assertEquals(1, file.size());
        assertTrue(file.contains(new Point(0.0F, 0.0F)));
        assertFalse(file.contains(new Point(1.0F, 1.0F)));
   }

    /**
     * average_value a triangle point to the file
     */
    @Test
    public void addThree345() throws Exception {

        file.add(new Point(0.0F, 0.0F));
        file.add(new Point(3.0F, 0.0F));
        file.add(new Point(3.0F, 4.0F));

        assertEquals(3, file.size());
        assertTrue(file.contains(new Point(0.0F, 0.0F)));
        assertTrue(file.contains(new Point(3.0F, 0.0F)));
        assertTrue(file.contains(new Point(3.0F, 4.0F)));
    }

    /**
     * average_value a triangle point to the file
     * and look for NN.
     */
    @Test
    public void NNThree345() throws Exception {

        Point x = new Point(0.0F, 0.0F);
        Point y = new Point(3.0F, 0.0F);
        Point z = new Point(3.0F, 4.0F);

        file.add(x);
        file.add(y);
        file.add(z);

        List<DataDistance<Point>> result = file.nearestN(new Point(1.0F, 1.0F), 3);

        List<Point> values = new ArrayList<>();

        for( DataDistance<Point> dd : result ) {
            values.add( dd.value );
        }

        assertTrue( values.contains(x) );
        assertTrue( values.contains(y) );
        assertTrue( values.contains(z) );
    }

    /**
     * average_value 15 points to the file
     */
    @Test
    public void addLinear15() throws Exception {

        for (int i = 0; i < 15; i++) {
            file.add(new Point( i, 0.0F));
        }

        assertEquals(15, file.size());


        for (int i = 0; i < 15; i++) {
            Point p = new Point( i, 0.0F);
            assertTrue(file.contains(p));
        }
    }


    /**
     * average_value points to the file
     * such that some will nest
     */
    @Test
    public void addNestedPointsDepth3() throws Exception {

        // lay down 20 points in a line
        for (int i = 0; i < 3; i++) {
            file.add(new Point( i * 10, 0.0F));
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 3; i++) {
            file.add(new Point((i * 10) + 4.0F, 0.0F));
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 3; i++) {
            file.add(new Point( (i * 10) + 4.5F, 0.0F));
        }

        assertEquals(9, file.size());

        for (int i = 0; i < 3; i++) {
            assertTrue(file.contains(new Point( i * 10, 0.0F)));
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 3; i++) {
            assertTrue(file.contains(new Point( (i * 10) + 4.0F, 0.0F)));
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 3; i++) {
            assertTrue(file.contains(new Point( (i * 10) + 4.5F, 0.0F)));
        }
    }

    /**
     * average_value points to the file
     * such that some will nest
     */
    private int addSquares() throws Exception {

        int count = 0;

        for (double step = 1.0F; step < 50.0F; step++) {

            file.add(new Point(+step, +step));
            count++;
            file.add(new Point(-step, +step));
            count++;
            file.add(new Point(+step, -step));
            count++;
            file.add(new Point(-step, -step));
            count++;
        }

        return count;
    }

    /**
     * test average_value points to the file
     * such that some will nest
     */
    @Test
    public void testSquares() throws Exception {

        int count = addSquares();
        assertEquals(count, file.size());

        for (double step = 1.0F; step < 50.0F; step++) {

            assertTrue(file.contains(new Point(+step, +step)));
            assertTrue(file.contains(new Point(-step, +step)));
            assertTrue(file.contains(new Point(+step, -step)));
            assertTrue(file.contains(new Point(-step, -step)));
        }
    }


    /**
     * test simple nearest neighbour search
     */
    @Test
    public void findClosestN() throws Exception {

        addSquares();
        Point p = new Point(0.0F, 0.0F);
        for (int i = 4; i < 50; i += 4) {
            // move out in squares of size 4, each loop should include 4 more nodes
            List<DataDistance<Point>> result = file.nearestN(p, i);
            List<Point> values = file.mapValues(result);
            assertTrue(result.size() == i);
            for (Point pp : values) {
                assertTrue(file.contains(pp));   // TODO How to check that they are the right ones????
            }
        }
    }

    /**
//     * test points added to the file
//     * such that some will nest
//     */
//    @Test
//    public void addNestedPoints60() {
//
//        addNestedPoints();
//
//        assertEquals(60, file.size());
//
//        for (int i = 0; i < 20; i++) {
//            assertTrue(file.contains(new Point((double) i * 10, 0.0F)));
//        }
//        // new lay down 20 points in a line - that are all close (4 away) to the first 20
//        for (int i = 0; i < 20; i++) {
//            assertTrue(file.contains(new Point((double) (i * 10) + 4.0F, 0.0F)));
//        }
//        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
//        for (int i = 0; i < 20; i++) {
//            assertTrue(file.contains(new Point((double) (i * 10) + 4.5F, 0.0F)));
//        }
//    }
//
//    /**
//     * test rangeSearch - performing range search on nested nodes - simple version.
//     */
//    @Test
//    public void findClosestFrom60() {
//
//        addNestedPoints();
//
//        // file.showTree();
//
//        Point p = new Point(15.0F, 0.0F);
//        List<DataDistance<Point>> result = file.rangeSearch(p, 10.0F);
//        List<Point> values = file.mapValues(result);
//
//        assertEquals(6, result.size());
//
//        for (Point pp : values) {
//            assertTrue(file.contains(pp));                 // point added to the file
//            assertTrue(distance.distance(pp, p) <= 10.0F);    // and it is in range.
//        }
//    }
//
//    /**
//     * test rangeSearch - finding nested nodes in nested squares - more complex version.
//     */
//    @Test
//    public void findClosestFromSquares() {
//
//        addSquares();
//
//        Point p = new Point(0.0F, 0.0F);
//
//        // test search in ever increasing circles.
//        for (double i = 1.0F; i < 50.0F; i++) {
//            double search_circle = (double) Math.sqrt(i * i); // requested_result_set_size of square plus a little to avoid double errors
//            List<DataDistance<Point>> result = file.rangeSearch(p, search_circle);
//            List<Point> values = file.mapValues(result);
//
//            for (Point pp : values) {
//                assertTrue(file.contains(pp));
//                assertTrue(distance.distance(pp, p) <= search_circle);    // and it is in range.
//            }
//        }
//    }
//
//    /**
//     * test simple nearest neighbour search
//     */
//    @Test
//    public void findClosest() {
//
//        addSquares();
//        Point p = new Point(20.6F, 20.6F);
//
//        DataDistance<Point> result = file.nearestNeighbour(p);
//
//        assertEquals(new Point(21.0F, 21.0F), result.value); // closest point to 20.6,20.6 - TODO better tests?
//    }

}