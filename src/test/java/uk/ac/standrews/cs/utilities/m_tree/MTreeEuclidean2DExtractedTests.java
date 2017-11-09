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

import static org.junit.Assert.assertEquals;

public class MTreeEuclidean2DExtractedTests {

    private MTree<Point> tree;

    @Before
    public void setUp() throws Exception {

        tree = new MTree<>((Distance) new EuclideanDistance());
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
}
