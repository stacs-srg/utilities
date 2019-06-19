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
package uk.ac.standrews.cs.utilities.richard.dataSets.fileReaders;

import uk.ac.standrews.cs.utilities.metrics.implementation.CartesianPoint;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Richard Connor
 * 
 *         Creates a new instance of DataSet of CartesianPoint from the given
 *         filename. Can be used as a DataSet<CartesianPoint> or alternatively
 *         as an ArrayList<CartesianPoint> depending on context
 */
@SuppressWarnings("serial")
public class CartesianPointFileReader extends ArrayList<CartesianPoint> {
	//	implements DataSet<CartesianPoint> {

	protected int dimension = -1;
	private String filename;
	private Random rand = new Random(0);

	/**
	 * 
	 * 
	 * 
	 * @param fileName
	 *            the name of the file to be read
	 * @param headerLine
	 *            whether a header line is present or not; if it is, this is
	 *            just ignored
	 * 
	 * @throws Exception
	 *             if something goes wrong opening or reading the file
	 */
	public CartesianPointFileReader(String fileName, boolean headerLine)
			throws Exception {
		this.filename = fileName;
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(fileName));

			int lineNumber = 1;

			if (headerLine) {
				@SuppressWarnings("unused")
				String chuck = lr.readLine();
				lineNumber++;
			}

			for (String s = lr.readLine(); s != null; s = lr.readLine()) {
				if (this.dimension == -1) {
					this.dimension = getDimension(s, lineNumber);
				}
				add(new CartesianPoint(getRealVectorFromLine(s, lineNumber++,
						this.dimension)));

			}

			lr.close();
		} catch (FileNotFoundException e) {
			throw new Exception("can't open file: " + fileName);
		} catch (IOException e) {
			throw new Exception("can't read from file: " + fileName);
		} catch (Throwable t) {
			throw new Exception("problem with file " + fileName + " : "
					+ t.getMessage());
		}
	}

	/**
	 * 
	 * 
	 * 
	 * @param fileName
	 *            the name of the file to be read
	 * @param headerLine
	 *            whether a header line is present or not; if it is, this is
	 *            just ignored
	 * @param noOfLines
	 *            the number of lines to read from the file; if there are less
	 *            than this in the file, an exception is thrown
	 * 
	 * @throws Exception
	 *             if something goes wrong opening or reading the file
	 */

	public CartesianPointFileReader(String fileName, boolean headerLine,
			int noOfLines) throws Exception {
		this.filename = fileName;
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(fileName));

			int lineNumber = 1;

			if (headerLine) {
				@SuppressWarnings("unused")
				String chuck = lr.readLine();
				lineNumber++;
			}

			for (int i = 0; i < noOfLines; i++) {
				String s = lr.readLine();
				if (this.dimension == -1) {
					this.dimension = getDimension(s, lineNumber);
				}
				add(new CartesianPoint(getRealVectorFromLine(s, lineNumber++,
						this.dimension)));

			}

			lr.close();
		} catch (FileNotFoundException e) {
			throw new Exception("can't open file: " + fileName);
		} catch (IOException e) {
			throw new Exception("can't read from file: " + fileName);
		} catch (Throwable t) {
			throw new Exception("problem with file " + fileName + " : "
					+ t.getMessage());
		}
	}

	//@Override
	public String getDataSetName() {
		String res = "Vector File: " + this.filename;
		return res;
	}

	//@Override
	public boolean isFinite() {
		return true;
	}

	//@Override
	public CartesianPoint randomValue() {
		final int index = this.rand.nextInt(size());
		CartesianPoint ret = get(index);
		return ret;
	}

	private static int getDimension(String s, int lineNumber) throws Exception {
		int dim = 0;
		Scanner s1 = new Scanner(s);
		try {
			while (s1.nextDouble() <= Double.MAX_VALUE) {
				dim++;
			}
		} catch (Throwable t) {
			/*
			 * ugly termination when we can no longer read a double... not nice
			 * but pragmatically useful
			 */
		}
		if (dim == 0) {
			throw new Exception(
					"can't read double values from first line of file");
		}
		return dim;
	}

	private static double[] getRealVectorFromLine(String s, int lineNumber,
			int dim) throws Exception {
		Scanner s1 = new Scanner(s);
		double[] p = new double[dim];

		try {
			populateLineArray(s1, p, dim);
		} catch (Throwable t) {
			throw new Exception(
					"get vector: badly formed line in file at line "
							+ lineNumber);
		}
		if (s1.hasNextDouble()) {
			throw new Exception("too many data values in file at line "
					+ lineNumber);
		}
		return p;
	}

	protected static void populateLineArray(Scanner s1, double[] p,
			int dimension) {
		for (int i = 0; i < dimension; i++) {
			p[i] = s1.nextDouble();
		}
	}

	// @Override
	public String getDataSetShortName() {
		return "no short name...";
	}
}
