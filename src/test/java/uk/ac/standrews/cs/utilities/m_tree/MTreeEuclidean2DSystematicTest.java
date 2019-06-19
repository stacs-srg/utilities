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
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class MTreeEuclidean2DSystematicTest {

    private MTree<Point> tree;
    private BruteForceSimilaritySearch<Point> brute_force;

    private Random random;
    private List<Point> points;
    private final int number_of_points;
    private final int repetition_number;
    private final boolean duplicate_values;

    private static final double range = 100.0f;
    private static final long SEED = 3459873497234L;

    private final Comparator<? super DataDistance<Point>> distance_comparator = (Comparator<DataDistance<Point>>) (o1, o2) -> Double.compare(o1.distance, o2.distance);

    private static final double[] radii = new double[]{0.0f, 0.1f, 1.0f, 10.0f, 50.0f, 100.0f};

    public MTreeEuclidean2DSystematicTest(final int number_of_points, final int repetition_number, final boolean duplicate_values) {

        this.number_of_points = number_of_points;
        this.repetition_number = repetition_number;
        this.duplicate_values = duplicate_values;
    }

    @Before
    public void setUp()  {

        final Metric<Point> distance_metric = new EuclideanDistance();

        tree = new MTree<>(distance_metric);
        brute_force = new BruteForceSimilaritySearch<>(distance_metric);

        random = new Random(SEED * number_of_points * repetition_number);

        createPoints();
        addPoints();
    }

    private void createPoints() {

        points = new ArrayList<>();

        for (int i = 0; i < number_of_points; i++) {

            Point p = randomPoint();

            int number_of_insertions = 1;
            if (duplicate_values) number_of_insertions += random.nextInt(5);

            for (int j = 0; j < number_of_insertions; j++) {
                points.add(p);
            }
        }
    }

    private void addPoints() {

        for (final Point p : points) {

            tree.add(p);
            brute_force.add(p);
        }
    }

    private Point randomPoint() {

        return new Point(random.nextFloat() * range, random.nextFloat() * range);
    }

    @Test
    public void treeContainsPoints() {

        for (final Point p : points) {
            assertTrue(tree.contains(p));
        }
    }

    @Test
    public void treeSizeIsCorrect() {

        assertEquals(points.size(), tree.size());
    }

    @Test
    public void pointsAreTheirOwnNearestNeighbours() {

        for (final Point p : points) {
            assertEquals(p, tree.nearestNeighbour(p).value);
        }
    }

    @Test
    public void checkNearestNeighboursOfAllPoints() {

        for (final Point p : points) {
            checkNearestNeighboursOfPoint(p);
        }
    }

    @Test
    public void checkNeighboursWithinRangesForAllPoints() {

        for (final Point p : points) {
            checkNeighboursWithinRangesForPoint(p);
        }
    }

    @Test
    public void askingForTooManyNeighboursDoesntCrash() {

        for (final Point p : points) {
            tree.nearestN(p, points.size() + 1);
        }
    }

    private void checkNearestNeighboursOfPoint(final Point p) {

        for (int number_of_neighbours = 1; number_of_neighbours < points.size(); number_of_neighbours++) {

            final List<DataDistance<Point>> tree_distances = tree.nearestN(p, number_of_neighbours);
            final List<DataDistance<Point>> brute_force_distances = brute_force.nearestN(p, number_of_neighbours);

            assertEquals(number_of_neighbours, tree_distances.size());
            assertSameDistanceOrder(tree_distances, brute_force_distances);
        }
    }

    private void checkNeighboursWithinRangesForPoint(final Point p) {

        for (final double radius : radii) {

            final List<DataDistance<Point>> tree_distances = tree.rangeSearch(p, radius);
            final List<DataDistance<Point>> brute_force_distances = brute_force.rangeSearch(p, radius);

            // Sort the MTree results since they're not ordered by distance.
            tree_distances.sort(distance_comparator);

            assertSameDistanceOrder(tree_distances, brute_force_distances);

            for (final DataDistance<Point> data_distance : tree_distances) {
                assertTrue(data_distance.distance <= radius);
            }
        }
    }

    private static void assertSameDistanceOrder(final List<DataDistance<Point>> tree_distances, final List<DataDistance<Point>> brute_force_distances) {

        final int number_of_neighbours = tree_distances.size();
        assertEquals(number_of_neighbours, brute_force_distances.size());

        for (int i = 0; i < number_of_neighbours; i++) {

            // Check that the pair-wise distances are the same, not the actual points, since order of points is undefined
            // if there are pairs with equal distances.

            assertTrue(Math.abs(tree_distances.get(i).distance - brute_force_distances.get(i).distance) < MTree.EPSILON);
        }
    }
}
