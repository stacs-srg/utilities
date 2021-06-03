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
package uk.ac.standrews.cs.utilities.richard.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author newrichard
 * 
 *         This class allows the creation of a list, up to a finite length,
 *         ordered (small to large) according to a second parameter provided at
 *         each element addition. It is handy, for instance, for creating a list
 *         of the n closest matches to a reference object from within a
 *         collection; a new OrderedList of size n is created, each element
 *         tested is added along with its distance from the reference object,
 *         after which the best n matches (and comparators) can be retrieved.
 * 
 * @param <E>
 *            the type of the list element
 * @param <I>
 *            the type of the comparator, must extend Comparable
 */
public class OrderedList<E, I extends Comparable<I>> {

	private List<E> elements;
	private List<I> comparators;
	private int limit;

	private I threshold;

	/**
	 * 
	 * Creates a new OrderedList object
	 * 
	 * @param limit
	 *            the maximum number of elements and comparators kept for later
	 *            retrieval
	 */
	public OrderedList(int limit) {
		this.elements = new ArrayList<E>();
		this.comparators = new ArrayList<I>();
		this.limit = limit;
		this.threshold = null;
	}

	/**
	 * @param element
	 *            the element to be added
	 * @param comparator
	 *            the value which will be used to place the element in the
	 *            ordered list
	 */
	public void add(E element, I comparator) {
		if (this.threshold == null || comparator.compareTo(this.threshold) <= 0) {
			if (this.comparators.size() == 0) {
				this.comparators.add(comparator);
				this.elements.add(element);
			} else {
				int ptr = 0;
				/*
				 * advance ptr until the node it's pointing at is greater than
				 * or equal to the comparator, or past the end of the list
				 */
				while (ptr < this.comparators.size()
						&& this.comparators.get(ptr).compareTo(comparator) <= 0) {
					ptr++;
				}

				if (ptr < this.limit) {
					this.comparators.add(ptr, comparator);
					this.elements.add(ptr, element);
				}
				if (this.comparators.size() == this.limit) {
					this.threshold = this.comparators.get(this.limit - 1);
				}
				if (this.comparators.size() > this.limit) {
					this.threshold = this.comparators.get(this.limit - 1);
					this.comparators.remove(this.limit);
					this.elements.remove(this.limit);
				}
			}
		}
	}

	/**
	 * @return the threshold required for a new item to be inserted; this might
	 *         be more efficient than a call to add if boxing is required
	 *         boxing. The result is null until the list is up to size, then the
	 *         real threshold for insertion is returned so this can be checked
	 *         before a call to "add" is required
	 */
	public I getThreshold() {
		return this.threshold;
	}

	/**
	 * @return the n elements added with the smallest comparators
	 */
	public List<E> getList() {
		return this.elements;
	}

	/**
	 * @return the comparators of the elements added
	 */
	public List<I> getComparators() {
		return this.comparators;
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer("[");
		for (E e : this.elements) {
			res.append(e.toString() + ",");
		}
		res.setCharAt(res.length() - 1, ']');
		res.append(" [");
		for (I c : this.comparators) {
			res.append(c.toString() + ",");
		}
		res.setCharAt(res.length() - 1, ']');

		return res.toString();
	}

}
