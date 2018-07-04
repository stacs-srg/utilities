/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
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

/**
 * Used by Metrics to return data and distance from the data.
 * Created by al on 20/02/2017.
 */
@SuppressWarnings("WeakerAccess")
public class DataDistance<T> {

    public T value;
    public double distance;

    public DataDistance(T data, double distance) {
        this.value = data;
        this.distance = distance;
    }
}
