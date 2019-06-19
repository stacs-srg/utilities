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
package uk.ac.standrews.cs.utilities.richard.dataPoints.cartesian;

import uk.ac.standrews.cs.utilities.metrics.implementation.CartesianPoint;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

/**
 * @param <T>
 * @author Richard Connor
 * Changed by al to return doubles to fit in with Distance elsewhere
 */
public class Euclidean<T extends CartesianPoint> extends Metric<T> {

    public double calculateDistance(T x, T y) {

        double[] ys = y.getPoint();
        double acc = 0;
        int ptr = 0;
        for (double xVal : x.getPoint()) {
            final double diff = xVal - ys[ptr++];
            acc += diff * diff;
        }
        return normaliseArbitraryPositiveDistance(Math.sqrt(acc));
    }

    @Override
    public String getMetricName() {
        return "euc";
    }
}
