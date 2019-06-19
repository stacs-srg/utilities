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
package uk.ac.standrews.cs.utilities.richard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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
public class OrderedListAlt<E, I extends Comparable<I>> {

	private TreeMap<I, List<E>> rep;
	private int currentSize;

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
	public OrderedListAlt(int limit) {
		this.rep = new TreeMap<I, List<E>>();
		this.currentSize = 0;
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
			/*
			 * either the max size hasn't been reached, or this element is in by
			 * right
			 */
			if (this.rep.containsKey(comparator)) {
				this.rep.get(comparator).add(element);
			} else {
				ArrayList<E> l = new ArrayList<E>();
				l.add(element);
				this.rep.put(comparator, l);
			}
			currentSize++;

			if (currentSize > limit) {
				final Entry<I, List<E>> lastEntry = this.rep.lastEntry();
				List<E> last = lastEntry.getValue();
				if (last.size() == 1) {
					this.rep.remove(lastEntry.getKey());
				} else {
					last.remove(0);
				}
				this.threshold = this.rep.lastKey();
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
		List<E> res = new ArrayList<E>();
		for (List<E> v : this.rep.values()) {
			res.addAll(v);
		}
		return res;
	}

	/**
	 * @return the comparators of the elements added
	 */
	public List<I> getComparators() {
		List<I> res = new ArrayList<I>();
		for (I k : this.rep.keySet()) {
			for (int i = 0; i < this.rep.get(k).size(); i++) {
				res.add(k);
			}
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer("[");
		List<I> comps = this.getComparators();
		List<E> vals = this.getList();
		for (E e : vals) {
			res.append(e.toString() + ",");
		}
		res.setCharAt(res.length() - 1, ']');
		res.append(" [");
		for (I c : comps) {
			res.append(c.toString() + ",");
		}
		res.setCharAt(res.length() - 1, ']');

		return res.toString();
	}

}
