/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module digitising-scotland-utils.
 *
 * digitising-scotland-utils is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * digitising-scotland-utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with digitising-scotland-utils. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean;

import uk.ac.standrews.cs.utilities.m_tree.Distance;

/**
 * Created by graham on 22/03/2017.
 */
public class EuclideanDistance implements Distance<Point> {

    public float distance(Point p1, Point p2) {
        float xdistance = p1.x - p2.x;
        float ydistance = p1.y - p2.y;

        return (float) Math.sqrt((xdistance * xdistance) + (ydistance * ydistance));
    }

}

