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

import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.EuclideanDistance;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.Point;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class PositivesAndNegativesEuclideanClusteringExploration {

    private static final double range = 100.0f;
    private static final long SEED = 3459873497234L;
    private static final double[] radii = new double[]{0.0f, 0.1f, 1.0f, 10.0f, 50.0f, 100.0f};
    private final int number_of_points;
    private final int repetition_number;
    private final boolean duplicate_values;
    private final Comparator<? super DataDistance<Point>> distance_comparator = (Comparator<DataDistance<Point>>) (o1, o2) -> Double.compare(o1.distance, o2.distance);
    private MTree<Point> tree;
    private Random random;
    private List<Point> points;
    private Point positive_nucleus = new Point(range * 1.5f, range * 1.5f); // a double positive node around which to nucleate positive points.
    private Point negative_nucleus = new Point(-range * 1.5f, -range * 1.5f); // a double positive node around which to nucleate positive points.

    public PositivesAndNegativesEuclideanClusteringExploration(final int number_of_points, final int repetition_number, final boolean duplicate_values) throws Exception {

        this.number_of_points = number_of_points;
        this.repetition_number = repetition_number;
        this.duplicate_values = duplicate_values;

        setUp();
        tree.showTree();
        tree.showStructure().printStats();
    }

    public static void main(String[] args) throws Exception {
        PositivesAndNegativesEuclideanClusteringExploration pandn = new PositivesAndNegativesEuclideanClusteringExploration(30, 1, false);
    }

    public void setUp() {

        final Metric<Point> distance_metric = new EuclideanDistance();

        tree = new MTree<>(distance_metric);

        random = new Random(SEED * number_of_points * repetition_number);

        createPoints();
        addPoints();
    }

    /*
     * Create positive and negatives turn and turn about
     */
    private void createPoints() {

        points = new ArrayList<>();

        for (int i = 0; i < number_of_points; i++) {

            Point p1 = randomPositivePoint();
            Point p2 = randomNegativePoint();

            int number_of_insertions = 1;
            if (duplicate_values) number_of_insertions += random.nextInt(5);

            for (int j = 0; j < number_of_insertions; j++) {
                points.add(p1);
                points.add(p2);
            }
        }
    }

    /*
     * Create all positives and then all negatives
     */
    private void createPoints2() {

        points = new ArrayList<>();

        for (int i = 0; i < number_of_points; i++) {

            Point p1 = randomPositivePoint();

            int number_of_insertions = 1;
            if (duplicate_values) number_of_insertions += random.nextInt(5);

            for (int j = 0; j < number_of_insertions; j++) {
                points.add(p1);
            }
        }

        for (int i = 0; i < number_of_points; i++) {

            Point p2 = randomNegativePoint();

            int number_of_insertions = 1;
            if (duplicate_values) number_of_insertions += random.nextInt(5);

            for (int j = 0; j < number_of_insertions; j++) {
                points.add(p2);
            }
        }
    }

    private void addPoints() {

        for (final Point p : points) {

            tree.add(p);
        }
    }

    private Point randomPositivePoint() {

        return new Point(positive_nucleus.x + random.nextFloat() * range, positive_nucleus.y + random.nextFloat() * range);
    }

    private Point randomNegativePoint() {

        return new Point(negative_nucleus.x + random.nextFloat() * range, negative_nucleus.y + random.nextFloat() * range);
    }
}
