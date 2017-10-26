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
package uk.ac.standrews.cs.utilities.m_tree;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MTreeEuclidean2DTest {

    private MTree<Point> tree;
    private EuclideanDistance distance;

    @Before
    public void setUp() throws Exception {

        distance = new EuclideanDistance();
        tree = new MTree<>(distance);
    }

    /**
     * add a single point to the tree
     */
    @Test
    public void addNone() {

        assertEquals(0, tree.size());
        assertFalse(tree.contains(new Point(0.0F, 0.0F)));
    }

    /**
     * add a single point to the tree
     */
    @Test
    public void addOne() {

        tree.add(new Point(0.0F, 0.0F));

        assertEquals(1, tree.size());
        assertTrue(tree.contains(new Point(0.0F, 0.0F)));
        assertFalse(tree.contains(new Point(1.0F, 1.0F)));
    }

    /**
     * add 3 points to the tree - 3,4,5 triangle
     */
    @Test
    public void addThree345() {

        tree.add(new Point(0.0F, 0.0F));
        tree.add(new Point(3.0F, 0.0F));
        tree.add(new Point(3.0F, 4.0F));

        assertEquals(3, tree.size());
        assertTrue(tree.contains(new Point(0.0F, 0.0F)));
        assertTrue(tree.contains(new Point(3.0F, 0.0F)));
        assertTrue(tree.contains(new Point(3.0F, 4.0F)));
    }

    /**
     * add 15 points to the tree
     */
    @Test
    public void addLinear15() {

        for (int i = 0; i < 15; i++) {
            tree.add(new Point((double) i, 0.0F));
        }

        assertEquals(15, tree.size());

        for (int i = 0; i < 15; i++) {
            assertTrue(tree.contains(new Point((double) i, 0.0F)));
        }
    }

    /**
     * add points to the tree
     * such that some will nest
     */
    @Test
    public void addNestedPointsDepth3() {

        // lay down 20 points in a line
        for (int i = 0; i < 3; i++) {
            tree.add(new Point((double) i * 10, 0.0F));
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 3; i++) {
            tree.add(new Point((double) (i * 10) + 4.0F, 0.0F));
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 3; i++) {
            tree.add(new Point((double) (i * 10) + 4.5F, 0.0F));
        }

        assertEquals(9, tree.size());

        for (int i = 0; i < 3; i++) {
            assertTrue(tree.contains(new Point((double) i * 10, 0.0F)));
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 3; i++) {
            assertTrue(tree.contains(new Point((double) (i * 10) + 4.0F, 0.0F)));
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 3; i++) {
            assertTrue(tree.contains(new Point((double) (i * 10) + 4.5F, 0.0F)));
        }
    }

    /**
     * add points to the tree
     * such that some will nest
     */
    private int addSquares() {

        int count = 0;

        for (double coord = 1.0F; coord < 50.0F; coord++) {

            tree.add(new Point(+coord, +coord));
            count++;
            tree.add(new Point(-coord, +coord));
            count++;
            tree.add(new Point(+coord, -coord));
            count++;
            tree.add(new Point(-coord, -coord));
            count++;
        }

        return count;
    }

    /**
     * test add points to the tree
     * such that some will nest
     */
    @Test
    public void testSquares() {

        int count = addSquares();
        assertEquals(count, tree.size());

        for (double coord = 1.0F; coord < 50.0F; coord++) {

            assertTrue(tree.contains(new Point(+coord, +coord)));
            assertTrue(tree.contains(new Point(-coord, +coord)));
            assertTrue(tree.contains(new Point(+coord, -coord)));
            assertTrue(tree.contains(new Point(-coord, -coord)));
        }
    }

    /**
     * add points to the tree
     * such that some will nest
     */
    private void addNestedPoints() {

        // lay down 20 points in a line
        for (int i = 0; i < 20; i++) {
            tree.add(new Point((double) i * 10, 0.0F));
            //t.showTree();
            //tree.check_invariants();
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 20; i++) {
            tree.add(new Point((double) (i * 10) + 4.0F, 0.0F));
            //t.showTree();
            //tree.check_invariants();
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 20; i++) {
            tree.add(new Point((double) (i * 10) + 4.5F, 0.0F));
            //t.showTree();
            //tree.check_invariants();
        }
    }

    /**
     * test points added to the tree
     * such that some will nest
     */
    @Test
    public void addNestedPoints60() {

        addNestedPoints();

        assertEquals(60, tree.size());

        for (int i = 0; i < 20; i++) {
            assertTrue(tree.contains(new Point((double) i * 10, 0.0F)));
        }
        // new lay down 20 points in a line - that are all close (4 away) to the first 20
        for (int i = 0; i < 20; i++) {
            assertTrue(tree.contains(new Point((double) (i * 10) + 4.0F, 0.0F)));
        }
        // new lay down another 20 points in a line - that are all close (1 away) to the second 20
        for (int i = 0; i < 20; i++) {
            assertTrue(tree.contains(new Point((double) (i * 10) + 4.5F, 0.0F)));
        }
    }

    /**
     * test rangeSearch - performing range search on nested nodes - simple version.
     */
    @Test
    public void findClosestFrom60() {

        addNestedPoints();

        // tree.showTree();

        Point p = new Point(15.0F, 0.0F);
        List<DataDistance<Point>> result = tree.rangeSearch(p, 10.0F);
        List<Point> values = tree.mapValues(result);

        assertEquals(6,result.size());

        for (Point pp : values) {
            assertTrue(tree.contains(pp));                 // point added to the tree
            assertTrue(distance.distance(pp, p) <= 10.0F);    // and it is in range.
        }
    }

    /**
     * test rangeSearch - finding nested nodes in nested squares - more complex version.
     */
    @Test
    public void findClosestFromSquares() {

        addSquares();

        Point p = new Point(0.0F, 0.0F);

        // test search in ever increasing circles.
        for (double i = 1.0F; i < 50.0F; i++) {
            double search_circle = (double) Math.sqrt(i * i); // requested_result_set_size of square plus a little to avoid double errors
            List<DataDistance<Point>> result = tree.rangeSearch(p, search_circle);
            List<Point> values = tree.mapValues(result);

            for (Point pp : values) {
                assertTrue(tree.contains(pp));
                assertTrue(distance.distance(pp, p) <= search_circle);    // and it is in range.
            }
        }
    }

    /**
     * test simple nearest neighbour search
     */
    @Test
    public void findClosest() {

        addSquares();
        Point p = new Point(20.6F, 20.6F);

        DataDistance<Point> result = tree.nearestNeighbour(p);

        assertEquals(result.value, new Point(21.0F, 21.0F)); // closest point to 20.6,20.6 - TODO better tests?
    }

    /**
     * test simple nearest neighbour search
     */
    @Test
    public void findClosestN() {

        addSquares();
        Point p = new Point(0.0F, 0.0F);
        for (int i = 4; i < 50; i += 4) {
            // move out in squares of size 4, each loop should include 4 more nodes
            List<DataDistance<Point>> result = tree.nearestN(p, i);
            List<Point> values = tree.mapValues(result);
            assertTrue(result.size() == i);
            for (Point pp : values) {
                assertTrue(tree.contains(pp));   // TODO How to check that they are the right ones????
            }
        }
    }
}