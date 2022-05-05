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
package uk.ac.standrews.cs.utilities.measures.coreConcepts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Measure<T> {

    public abstract String getMeasureName();

    public abstract boolean maxDistanceIsOne();

    protected abstract double calculateDistance(T x, T y);

    /**
     * Calculates the distance between two points, without normalisation, so the result can take any non-negative value.
     * The distance is constrained to zero if the points are equal.
     *
     * @param x the first point
     * @param y the second point
     * @return the distance between the points
     */
    public double distance(T x, T y) {

        if (x.equals(y)) {
            return 0.0;
        }

        return calculateDistance(x, y);
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
