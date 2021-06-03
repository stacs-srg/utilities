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
package uk.ac.standrews.cs.utilities.metrics.coreConcepts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Metric<T> {

    public abstract String getMetricName();

    protected abstract double calculateDistance(T x, T y);

    public final double distance(T x, T y) {

        final double result = calculateDistance(x, y);
        if (result < 0.0 || result > 1.0) throw new RuntimeException("non-normalised distance: " + result);

        return result;
    }

    /**
     * @param distance - the distance to be normalised
     * @return the distance in the range 0-1:  1 - ( 1 / d + 1 )
     */
    protected static double normaliseArbitraryPositiveDistance(double distance) {
        return 1d - (1d / (distance + 1d));
    }

    public static <T> Set<T> union(Collection<T> a, Collection<T> b) {

        Set<T> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    public static <T> Set<T> intersection(Collection<T> a, Collection<T> b) {

        Set<T> result = new HashSet<>(a);
        result.retainAll(b);

        return result;
    }
}
