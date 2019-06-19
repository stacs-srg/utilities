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
package uk.ac.standrews.cs.utilities.metrics.coreConcepts;

public class CountedMetric<T> extends Metric<T> {

	int count;
	Metric<T> m;

	public CountedMetric(Metric<T> m) {

		count = 0;
		this.m = m;
	}

	@Override
	public double calculateDistance(T x, T y) {

		count++;
		return m.distance(x, y);
	}

	@Override
	public String getMetricName() {
		return m.getMetricName();
	}

	public int reset() {

		int res = count;
		count = 0;
		return res;
	}

	public int getComparisonCount() {
		return count;
	}
}
