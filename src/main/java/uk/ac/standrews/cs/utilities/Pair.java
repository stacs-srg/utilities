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
package uk.ac.standrews.cs.utilities;

/**
 * This class represents a 2-tuple.
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
@SuppressWarnings("WeakerAccess")
public class Pair<X, Y> {

    private final X x;
    private final Y y;

    /**
     * Construct a pair of two values.
     * Examples:
     * - new Pair<>(1, 2)
     * - new Pair<>("Hello", 2)
     * - new Pair<>("Hello", "World")
     *
     * @param x the first element of the pair
     * @param y the second element of the pair
     */
    @SuppressWarnings("WeakerAccess")
    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the first element of the pair
     *
     * @return X
     */
    @SuppressWarnings("WeakerAccess")
    public X X() {
        return x;
    }

    /**
     * Get the second element of the pair
     *
     * @return Y
     */
    @SuppressWarnings("WeakerAccess")
    public Y Y() {
        return y;
    }
}
