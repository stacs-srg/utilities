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
package uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class EuclideanDistance extends Metric<Point> {

    public double calculateDistance(Point p1, Point p2) {

        double x_distance = p1.x - p2.x;
        double y_distance = p1.y - p2.y;

        return normaliseArbitraryPositiveDistance(Math.sqrt((x_distance * x_distance) + (y_distance * y_distance)));
    }

    @Override
    public String getMetricName() {
        return "EuclideanDistance (2D)";
    }
}
