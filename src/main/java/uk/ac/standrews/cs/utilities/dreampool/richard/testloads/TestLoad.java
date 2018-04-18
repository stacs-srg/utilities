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

package uk.ac.standrews.cs.utilities.dreampool.richard.testloads;

import uk.ac.standrews.cs.utilities.dreampool.richard.coreConcepts.DataSet;
import uk.ac.standrews.cs.utilities.dreampool.richard.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.dreampool.richard.dataSets.fileReaders.CartesianPointFileReader;
import uk.ac.standrews.cs.utilities.dreampool.richard.dataSets.fileReaders.generators.CartesianPointGenerator;
import uk.ac.standrews.cs.utilities.dreampool.richard.searchStructures.SearchIndex;
import uk.ac.standrews.cs.utilities.dreampool.richard.util.OrderedList;
import uk.ac.standrews.cs.utilities.metrics.CartesianPoint;

import java.util.ArrayList;
import java.util.List;

public class TestLoad {

	public enum LoadType {
		file, gen
	}

	public enum SisapFile {
		colors, nasa
	};

	public static double[] getSisapThresholds(SisapFile file) {
		/*
		 * TODO
		 * 
		 * very very temp!
		 */
		double[] colorsMine = { 0.062744569, 0.079139633, 0.09994441,
				0.126288016, 0.158184747 };
		double[] nasaMine = { 0.3021816169379105, 0.3911328640777959,
				0.4742702901844148, 0.5567372856302092, 0.6667229088576696 };
		double[] colorsEdgars = { 0.051768, 0.082514, 0.131163 };
		double[] colorsLowerTest = { 0.02, 0.03, 0.04 };
		double[] nasaEdgars = { 0.12, 0.285, 0.53 };
		if (file == SisapFile.colors) {
			return colorsEdgars;
		} else {
			return nasaEdgars;
		}
	};

	protected static double getThreshold(Metric<CartesianPoint> m,
			CartesianPoint query, int req, double thresh,
			SearchIndex<CartesianPoint> vpt) {
		List<CartesianPoint> res = vpt.thresholdSearch(query, thresh);
		while (res.size() < req) {
			thresh *= 1.1;
			res = vpt.thresholdSearch(query, thresh);
		}
		while (res.size() > req) {
			res = vpt.thresholdSearch(query, thresh / 1.05);
			if (!(res.size() <= req)) {
				thresh /= 1.05;
			}
		}
		res = vpt.thresholdSearch(query, thresh);
		OrderedList<CartesianPoint, Float> topReq = new OrderedList<>(req);
		for (CartesianPoint d : res) {
			topReq.add(d, m.distance(d, query));
		}
		double dist = topReq.getComparators().get(req - 1);
		dist *= 1.001;
		return dist;
	}

	public static String SISAP_PATH = "sisap_data";
	private List<CartesianPoint> testData;
	private List<CartesianPoint> testQueries;

	/**
	 * 
	 * generated cartesian data
	 * 
	 * @param dimension
	 * @param size
	 */
	public TestLoad(int dimension, int size, boolean gaussian) {
		this.testData = new ArrayList<>();
		DataSet<CartesianPoint> ds = new CartesianPointGenerator(dimension,
				gaussian);
		for (int i = 0; i < size; i++) {
			this.testData.add(ds.randomValue());
		}
	}

	/**
	 * 
	 * generated cartesian data
	 * 
	 * @param dimension
	 * @param size
	 * @param repeatable
	 * @param gaussian
	 */
	public TestLoad(int dimension, int size, boolean repeatable,
			boolean gaussian) {
		this.testData = new ArrayList<>();
		CartesianPointGenerator ds;
		if (repeatable) {
			ds = new CartesianPointGenerator(dimension, gaussian);
		} else {
			ds = new CartesianPointGenerator(dimension, false, gaussian);
		}
		for (int i = 0; i < size; i++) {
			this.testData.add(ds.randomValue());
		}
	}

	public TestLoad(SisapFile file) throws Exception {
		super();
		switch (file) {
		case colors: {
			this.testData = new CartesianPointFileReader(SISAP_PATH
					+ "/vectors/colors/colors.ascii", true);
		}
			;
			break;
		case nasa: {
			this.testData = new CartesianPointFileReader(SISAP_PATH
					+ "/vectors/nasa/nasa.ascii", true);
		}
			;
			break;
		default: {
			throw new Exception("unknown data set " + file);
		}
		}
	}

	public int dataSize() {
		return this.testData.size();
	}

	/**
	 * 
	 * For a given metric and query, returns a threshold value just slightly
	 * bigger than that required to get a given percentage of the data
	 * 
	 * @param m
	 * @param queries
	 * @param returns
	 * @param guess
	 *            A guess at the correct value; works anyway, but will be faster
	 *            with a good guess
	 * @return 0.1 percent more than the threshold at which the last datum lies
	 *         so guaranteed to return at least the correct number, may return
	 *         more in a very dense and large space
	 */
	@SuppressWarnings("boxing")
//	public List<Double> findThresholdByPercent(Metric<CartesianPoint> m,
//			List<CartesianPoint> queries, int[] returns, double guess) {
//
//		assert m != null;
//		double thresh = guess;
//		List<Double> res = new ArrayList<>();
//		VPTree<CartesianPoint> vpt = new VPTree<>(this.getDataCopy(), m);
//		for (int req : returns) {
//			double acc = 0;
//			for (CartesianPoint query : queries) {
//				double dist = getThreshold(m, query, req, thresh, vpt);
//				acc += dist;
//				thresh = dist;
//			}
//			res.add(acc / queries.size());
//		}
//		return res;
//	}

	public List<CartesianPoint> getDataCopy() {
		List<CartesianPoint> res = new ArrayList<>();
		for (CartesianPoint p : this.testData) {
			res.add(p);
		}
		return res;
	}

	public List<CartesianPoint> getQueries(int number) {
		List<CartesianPoint> res = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			res.add(this.testData.remove(0));
		}
		this.testQueries = res;
		return res;
	}

	public List<CartesianPoint> getQueriesCopy() {
		return this.testQueries;
	}

}
