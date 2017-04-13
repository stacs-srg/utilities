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
package uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean;

public class Point {

    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    private final static float epsilon = 0.00000000001F;

    public String toString() {
        return "[" + x + "," + y + "]";
    }

    public boolean equals(Object o) {

        if (o instanceof Point) {
            Point p = (Point) o;
            return this.x == p.x && this.y == p.y;
            // return (Math.abs(this.x - p.x) < epsilon) && (Math.abs(this.y - p.y) < epsilon);
        } else return false;
    }
}
