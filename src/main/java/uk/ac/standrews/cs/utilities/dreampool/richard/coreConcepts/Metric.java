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

package uk.ac.standrews.cs.utilities.dreampool.richard.coreConcepts;

/**
 * @author Richard Connor
 * 
 *         an interface describing a distance metric, normally used with the
 *         intent of creating a MetricSpace object. Note that this should be a
 *         true metric otherwise some other classes will not function correctly
 * 
 * @param <T>
 *            the type over which the metric operates
 */
public interface Metric<T> {

	/**
	 * the distance function of the metric
	 * 
	 * this should be a proper metric, ie a positive function with symmetry,
	 * psuedo-identity and triangle inequality over the space of type T objects
	 * 
	 * @param x
	 *            the first parameter
	 * @param y
	 *            the second parameter
	 * @return the distance between the parameters
	 */
	public float distance(T x, T y);

	/**
	 * it is important to give a useful name here so that the output of
	 * experimental systems is correctly interpreted
	 * 
	 * @return a name for a metric
	 */
	public String getMetricName();

}
