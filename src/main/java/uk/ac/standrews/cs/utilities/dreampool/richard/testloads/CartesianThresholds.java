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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author newrichard
 *
 *         the map is from dimensions to metric to results per million ->
 *         threshold
 */
public class CartesianThresholds extends
		TreeMap<Integer, Map<String, Map<Integer, Double>>> {

	protected static void setMap(Map<String, double[]> res, double[] cos,
			double[] euc, double[] jsd, double[] man, double[] sed, double[] tri) {
		res.put("cos", cos);
		res.put("euc", euc);
		res.put("jsd", jsd);
		res.put("man", man);
		res.put("sed", sed);
		res.put("tri", tri);
	}

	public static String[] metrics = { "cos", "euc", "jsd", "man", "sed", "tri" };

	public static int[] dims = { 6, 8, 10, 12, 14 };

	public static int[] perMil = { 1, 2, 4, 8, 16, 32 };

	// test thresholds in 8 dimensions,,,,,,};
	// results per million:,1,2,4,8,16,32};

	// test thresholds in 10 dimensions,,,,,,};
	// results per million:,1,2,4,8,16,32};
	// test thresholds in 12 dimensions,,,,,,};
	// results per million:,1,2,4,8,16,32};
	// test thresholds in 14 dimensions,,,,,,};
	// results per million:,1,2,4,8,16,32};

	/**
	 * creates a new map from dimensions to metric short name to results per
	 * million to threshold
	 */
	@SuppressWarnings("boxing")
	public CartesianThresholds() {
		for (int dim : dims) {
			Map<String, Map<Integer, Double>> perDimMap = new TreeMap<>();
			Map<String, double[]> pd = perDim(dim);
			for (String metric : pd.keySet()) {
				Map<Integer, Double> endMap = new TreeMap<>();
				double[] ts = pd.get(metric);
				int ptr = 0;
				for (int pm : perMil) {
					endMap.put(pm, ts[ptr++]);
				}
				perDimMap.put(metric, endMap);
			}
			this.put(dim, perDimMap);
		}
	}

	private Map<String, double[]> perDim(int dim) {
		Map<String, double[]> res = new HashMap<>();
		switch (dim) {
		case 6: {
			double[] cos = { 0.022814911, 0.027158715, 0.032297407,
					0.037294081, 0.04291203, 0.049542234 };
			double[] euc = { 0.071574561, 0.084626714, 0.096464512,
					0.110991854, 0.126392107, 0.14361292 };
			double[] jsd = { 0.022050762, 0.026255424, 0.030448416,
					0.035367311, 0.040763173, 0.046973768 };
			double[] man = { 0.139519307, 0.165415282, 0.189378132,
					0.217013099, 0.248662201, 0.28195792 };
			double[] sed = { 0.020984499, 0.024852929, 0.028685232,
					0.033154991, 0.038033613, 0.04362651 };
			double[] tri = { 0.025934027, 0.03087726, 0.035810661, 0.041554266,
					0.047833815, 0.055096235 };
			setMap(res, cos, euc, jsd, man, sed, tri);
		}
			break;
		case 8: {
			double[] cos = { 0.047353751, 0.054280985, 0.061660899,
					0.069238151, 0.07704599, 0.085742187 };
			double[] euc = { 0.144614658, 0.168200701, 0.187512099,
					0.205576804, 0.227902546, 0.250637033 };
			double[] jsd = { 0.045692953, 0.051392365, 0.057988126,
					0.064545835, 0.07127745, 0.078661059 };
			double[] man = { 0.3247043, 0.371121395, 0.415018981, 0.459836323,
					0.505832056, 0.558355378 };
			double[] sed = { 0.042455425, 0.047579833, 0.053486866,
					0.059333733, 0.065318244, 0.071865107 };
			double[] tri = { 0.053627417, 0.060218648, 0.068007941,
					0.075502655, 0.083364031, 0.09196574 };
			setMap(res, cos, euc, jsd, man, sed, tri);
		}
			;
			break;

		case 10: {
			double[] cos = { 0.073281302, 0.080839233, 0.088679407,
					0.097150146, 0.106222636, 0.115488817 };
			double[] euc = { 0.235107454, 0.261581263, 0.284152313,
					0.309255879, 0.335066748, 0.361365131 };
			double[] jsd = { 0.067187667, 0.073747476, 0.079974622,
					0.086983113, 0.09458939, 0.102641205 };
			double[] man = { 0.588083083, 0.650552106, 0.705277971,
					0.765042802, 0.826039055, 0.892327589 };
			double[] sed = { 0.061688694, 0.067519071, 0.073036912, 0.07923404,
					0.085942621, 0.093030787 };
			double[] tri = { 0.07865173, 0.086295293, 0.093488409, 0.101631315,
					0.110407915, 0.119593361 };
			setMap(res, cos, euc, jsd, man, sed, tri);
		}
			;
			break;

		case 12: {
			double[] cos = { 0.095472193, 0.10417436, 0.113729162, 0.122072716,
					0.130857597, 0.14021532 };
			double[] euc = { 0.325980644, 0.354601837, 0.380481633,
					0.407209687, 0.437586224, 0.468386812 };
			double[] jsd = { 0.084140862, 0.09162944, 0.099573498, 0.107264899,
					0.114912206, 0.122858758 };
			double[] man = { 0.878160908, 0.947717212, 1.027683139,
					1.095160671, 1.174395751, 1.257971614 };
			double[] sed = { 0.076723421, 0.083338258, 0.090337228,
					0.097100346, 0.10381492, 0.11078182 };
			double[] tri = { 0.098303461, 0.106900095, 0.11613873, 0.12502801,
					0.133676296, 0.142765337 };
			setMap(res, cos, euc, jsd, man, sed, tri);
		}
			;
			break;

		case 14: {
			double[] cos = { 0.118519975, 0.125656596, 0.134917523,
					0.143825521, 0.153104754, 0.162804695 };
			double[] euc = { 0.420376768, 0.448957314, 0.478534336, 0.51090981,
					0.543252022, 0.575408341 };
			double[] jsd = { 0.103732519, 0.111241813, 0.118602468,
					0.126081232, 0.133711538, 0.141625561 };
			double[] man = { 1.219428846, 1.304028505, 1.389503468,
					1.481624892, 1.569473238, 1.665796214 };
			double[] sed = { 0.093989841, 0.100589829, 0.107050388,
					0.113603783, 0.120284998, 0.127207882 };
			double[] tri = { 0.120964494, 0.129429273, 0.137973061,
					0.146633897, 0.155219185, 0.16417371 };
			setMap(res, cos, euc, jsd, man, sed, tri);
		}
			;
			break;
		}

		return res;
	}

	public static void main(String[] a) {
		for (int dim = 1; dim < 20; dim++) {
			System.out.print("dim");
			for (int ppm : CartesianThresholds.perMil) {
				System.out.print("\t" + getThreshold("euc", dim, ppm));
			}
			System.out.println();
		}
	}

	@SuppressWarnings("boxing")
	public static double getThreshold(String metric, int dims, int ppm) {
		if (metric.equals("euc")) {
			return euclideanRadius(dims, ppm * 0.000001);
		} else {
			CartesianThresholds ct = new CartesianThresholds();
			return ct.get(dims).get(metric).get(ppm);
		}
	}

	public static double euclideanRadius(int dim, double vol) {
		if (dim % 2 == 0) {
			return getEvenRadius(dim, vol);
		} else {
			return getOddRadius(dim, vol);
		}
	}

	private static double getOddRadius(int dim, double vol) {
		int k = dim / 2;
		final int dFac = dFactorial(dim);
		double kFacV = dFac * vol;
		double bot = Math.pow(2, k + 1) * Math.pow(Math.PI, k);
		double frac = kFacV / bot;
		// System.out.println(k + "\t" + dim + "\t" + vol + "\t" + dFac + "\t"
		// + bot);
		final double doubleDims = (double) 1 / dim;
		// System.out.println(doubleDims);
		return Math.pow(frac, doubleDims);
	}

	private static double getEvenRadius(int dim, double vol) {
		int k = dim / 2;
		double kFacV = factorial(k) * vol;
		double top = Math.pow(kFacV, (double) 1 / (2 * k));
		double bot = Math.sqrt(Math.PI);
		return top / bot;
	}

	private static int factorial(int k) {
		if (k == 1) {
			return 1;
		} else {
			return k * factorial(k - 1);
		}
	}

	private static int dFactorial(int k) {
		if (k == 1) {
			return 1;
		} else {
			return k * dFactorial(k - 2);
		}
	}

}
