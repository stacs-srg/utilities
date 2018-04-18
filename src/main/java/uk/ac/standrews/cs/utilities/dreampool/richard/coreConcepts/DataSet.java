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
 *         An interface to capture the notion of a set of data, normally with
 *         the intention of creating a MetricSpace object. The set may be
 *         finite, eg read from a file, or generated, and is therefore not a
 *         proper set, ie duplicates may occur without necessarily being an
 *         error.
 * 
 *         If isFinite returns false, then size should return -1 and the
 *         for-loop Iterator syntax should not be used!
 * 
 * @param <T>
 *            the type of the objects in the data set
 */
public interface DataSet<T> extends Iterable<T> {

	/**
	 * @return true for a finite set, false for an unbounded (probably
	 *         generated) set
	 */
	boolean isFinite();

	/**
	 * @return a random value from the set. If the set is eg read from a
	 *         pre-randomised file, this may be implemented as just the next
	 *         value. It is good practice for classes implementing this to give
	 *         repeated streams of values from successive calls for each
	 *         instance creation, to allow experiments to be reliably repeated.
	 *         If this behaviour is not desirable for a given task, then this
	 *         should be explicitly built into the constructor.
	 */
	T randomValue();


	/**
	 * it is important to give a useful name here so that the output of
	 * experimental systems is correctly interpreted
	 * 
	 * @return a name for the data set
	 */
	String getDataSetName();
	
	/**
	 * a short name that can be used in file names etc
	 * 
	 * @return a name for the data set
	 */
	String getDataSetShortName();

	/**
	 * @return the size of the collection, or -1 for an unbounded generator
	 */
	int size();

}
