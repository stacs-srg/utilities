/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
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
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MTreeEuclidean2DExtractedTests {

    private MTree<Point> tree;

    @Before
    public void setUp() throws Exception {

        tree = new MTree<>(new EuclideanDistance());
    }

    @Test
    public void expectedNumberOfNearestNeighbours1() {

        final Point[] points = new Point[]{new Point(73f, 83f), new Point(24f, 60f), new Point(63f, 30f)};

        for (final Point p : points) {
            tree.add(p);
        }

        assertEquals(2, tree.nearestN(new Point(74f,85f), 2).size());
    }

    @Test
    public void expectedNumberOfNearestNeighbours2() {

        final Point[] points = new Point[]{new Point(59f, 34f), new Point(39f, 82f), new Point(53f, 16f), new Point(52f, 27f), new Point(11f, 82f)};

        for (final Point p : points) {
            tree.add(p);
        }

        assertEquals(4, tree.nearestN(new Point(59f,34f), 4).size());
    }

    @Test
    public void nearestNeighbour() {

        final Point[] points = new Point[]{new Point(73.0f,83.0f), new Point(24.0f,60.0f)};

        for (final Point p : points) {
            tree.add(p);
        }

        Point query = new Point(25.0f, 60.0f);
        final List<DataDistance<Point>> tree_distances = tree.nearestN(query, 1);
        DataDistance<Point> nearest = tree.nearestNeighbour(query);

        assertEquals(1, tree_distances.size());

        assertEquals(new Point(24.0f,60.0f), tree_distances.get(0).value);
    }

    @Test
    public void complexContainmentCase() {

        // This was extracted from the systematic tests.

        final Point[] points = new Point[]{

                new Point(82.4229f,58.95196f),
                new Point(98.735245f,69.65837f),
                new Point(27.537685f,47.00716f),
                new Point(27.537685f,47.00716f),
                new Point(27.537685f,47.00716f),
                new Point(27.537685f,47.00716f),
                new Point(20.639742f,51.008736f),
                new Point(36.022717f,96.24436f),
                new Point(59.27654f,66.8644f),
                new Point(33.103413f,44.049835f),
                new Point(11.747485f,95.73507f),
                new Point(72.83516f,97.15104f),
                new Point(83.123535f,48.2668f),
                new Point(89.52559f,51.824333f),
                new Point(67.30614f,25.55248f),
                new Point(87.24511f,66.60964f),
                new Point(22.897804f,3.764838f),
                new Point(55.620594f,1.3766944f),
                new Point(91.21355f,85.75048f),
                new Point(99.950935f,17.971336f),
                new Point(49.811234f,72.86885f),
                new Point(43.795223f,56.088f),
                new Point(80.95218f,34.72701f),
                new Point(27.311684f,45.6188f),
                new Point(75.81781f,66.77986f),
                new Point(37.663567f,10.39406f),
                new Point(57.304836f,84.31088f),
                new Point(38.591938f,69.45116f),
                new Point(30.602991f,32.76459f),
                new Point(33.555405f,0.51376224f),
                new Point(12.365442f,20.824778f),
                new Point(62.78216f,84.463455f),
                new Point(14.25696f,27.99599f),
                new Point(57.527233f,53.373413f),
                new Point(99.824265f,89.8802f),
                new Point(38.140976f,10.149556f),
                new Point(51.662445f,16.481287f),
                new Point(33.765347f,49.02065f),
                new Point(25.102402f,13.876563f),
                new Point(38.794346f,15.708971f),
                new Point(54.297173f,64.28842f),
                new Point(34.485847f,21.022808f),
                new Point(75.25235f,5.2195015f)
        };

        for (final Point p : points) {
            tree.add(p);
        }

        assertTrue(tree.contains(new Point(20.639742f,51.008736f)));
    }
}
