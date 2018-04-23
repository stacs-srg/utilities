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
package uk.ac.standrews.cs.utilities.dreampool.richard.util;

import java.util.Iterator;

public class Range implements Iterable<Integer> {

	Iterator<Integer> i;

	/**
	 * @param start
	 *            of range (inclusive)
	 * @param upb
	 *            end of range (exclusive)
	 * 
	 *            returns an Iterable<Integer> object which returns integer
	 *            values from lwb (inclusive) to upb (exclusive) ; just
	 *            syntactic sugar to avoid the now-horrible use of for loops
	 *            over integers!
	 */
	public Range(final int lwb, final int upb) {
		final int[] box = { lwb };
		this.i = new Iterator<Integer>() {
			@Override
			public boolean hasNext() {
				return box[0] < upb;
			}

			@SuppressWarnings("boxing")
			@Override
			public Integer next() {
				return box[0]++;
			}
		};
	}

	public static Range range(int lwb, int upb) {
		return new Range(lwb, upb);
	}

	@Override
	public Iterator<Integer> iterator() {
		return this.i;
	}
}
