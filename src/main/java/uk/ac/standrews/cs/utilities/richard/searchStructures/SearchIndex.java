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
package uk.ac.standrews.cs.utilities.richard.searchStructures;

import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;

import java.util.*;

public abstract class SearchIndex<T> {

	public Measure<T> measure;
	public List<T> data;
	protected Random rand;

	protected SearchIndex(List<T> data, Measure<T> measure) {
		this.measure = measure;
		this.data = data;
		this.rand = new Random(0);
	}

	private List<T> chooseTwoPivotsOutliers(List<T> dat, int iterations) {
		List<T> res = new ArrayList<>();

		final int r1 = rand.nextInt(dat.size());
		T contendor1 = dat.get(r1);
		T contendor2 = findFarthest(contendor1, dat);

		assert contendor2 != null;
		for (int i = 0; i < iterations; i++) {
			contendor1 = contendor2;
			contendor2 = findFarthest(contendor1, dat);
		}
		if (contendor1 == contendor2) {
			throw new RuntimeException(
					"failed to extract two different pivots, " + dat.size());
		}
		res.add(contendor1);
		res.add(contendor2);
		dat.remove(contendor1);
		dat.remove(contendor2);
		return res;
	}

	private T findFarthest(T condendor, List<T> dat) {
		assert condendor != null : "findFarthest: contendor is null";
		double max = 0;
		T furthest = null;
		for (T d : dat) {
			if (d != condendor) {
				double dist = measure.distance(condendor, d);
				if (dist >= max) {
					furthest = d;
					max = dist;
				}
			}
		}
		return furthest;
	}

	private T findNearest(T condendor1, Collection<T> dat) {
		double minDist = Double.MAX_VALUE;
		T nearest = null;
		for (T d : dat) {
			if (d != condendor1) {
				double dist = measure.distance(condendor1, d);
				if (dist <= minDist) {
					nearest = d;
					minDist = dist;
				}
			}
		}
		return nearest;
	}

	protected List<T> chooseTwoPivots(List<T> dat) {
		return chooseTwoPivotsOutliers(dat, 1);
	}

	protected T chooseFurthestPivot(T fixed, List<T> dat) {
		final T furthest = findFarthest(fixed, dat);
		dat.remove(furthest);
		return furthest;
	}

	public T chooseBestPivot(Collection<T> collection, List<T> dat) {

		double bestTotal = 0;
		T bestPivot = null;
		for (T contendor : dat) {
			double total = 0;
			for (T pivot : collection) {
				double d = measure.distance(contendor, pivot);
				total += d;
			}
			if (total > bestTotal) {
				bestTotal = total;
				bestPivot = contendor;
			}
		}
		return bestPivot;
	}

	protected T chooseMaxiMinPivot(Set<T> pivots, List<T> dat) {
		/*
		 * we have a set of existing pivot points, and a list from which to
		 * choose a new one. The strategy is to find the one which has the
		 * maximum least distance, starting by finding the farthest from each
		 */
		List<T> farthest = new ArrayList<>();
		for (T p : pivots) {
			farthest.add(findFarthest(p, dat));
		}
		// for each element i of pivots, farthest[i] is the data point farthest
		// away
		double[] mins = new double[farthest.size()];
		for (int i = 0; i < mins.length; i++) {
			double min = Double.MAX_VALUE;
			for (T d : farthest) {
				T closestPivot = findNearest(d, pivots);
			}
			mins[i] = min;
		}
		int maxMin = 0;
		double max = 0;
		for (int i = 0; i < mins.length; i++) {
			double m = mins[i];
			if (m > max) {
				max = m;
				maxMin = i;
			}
		}
		return null;
	}

	protected T chooseNearestPivot(T fixed, List<T> dat) {
		final T nearest = findNearest(fixed, dat);
		dat.remove(nearest);
		return nearest;
	}

	/**
	 * for three line lengths of the triangle aqb, this gives the distance from
	 * "dropping the perpendicular" from q to ab; nb it can be negative if qb is
	 * greater than ab
	 */
	public static double projectionDistance(double ab, double aq, double bq) {
		if (ab == 0) {
			throw new RuntimeException("coincident reference points");
		}
		if (aq == 0) {
			return 0;
		} else if (bq == 0) {
			return aq;
		} else {
			return (ab * ab + aq * aq - bq * bq) / (2 * ab);
		}
	}

	/**
	 * for three points p1, p2 and s; gives the perpendicular distance from p1p2
	 * to s, given the lenght of p1s and the projection of s onto p1p2
	 */
	public static double altitude(double proj, double aq) {
		return Math.sqrt(aq * aq - proj * proj);
	}

	/**
	 * @param query
	 *            the query
	 * @param t
	 *            the threshold
	 * @return the list of items within the threshold distance of the query
	 */
	public abstract List<T> thresholdSearch(T query, double t);

	/**
	 * @return short abbreviation of the index name
	 */
	public abstract String getShortName();

	protected double listMaxDist(T t, List<T> list) {
		double res = 0;
		for (T d : list) {
			res = Math.max(res, measure.distance(t, d));
		}
		return res;
	}
}
