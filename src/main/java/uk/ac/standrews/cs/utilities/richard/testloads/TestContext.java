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
package uk.ac.standrews.cs.utilities.richard.testloads;

import uk.ac.standrews.cs.utilities.measures.implementation.CartesianPoint;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;
import uk.ac.standrews.cs.utilities.richard.dataPoints.cartesian.Euclidean;

import java.util.List;

/**
 * idea is to create, with as little syntactic fuss as possible, various test
 * loads useful for measuring metric space indexing techniques
 *
 * @author Richard Connor
 */
public class TestContext {

    private Context context;

    private TestLoad tl;
    private List<CartesianPoint> queries;
    private List<CartesianPoint> refPoints;
    private Measure<CartesianPoint> measure;
    private double threshold;

    /**
     * standard 10 dimensional test context, most commonly used!
     *
     * @param size the size of the dataset created
     * @throws Exception
     */
    public TestContext(int size) throws Exception {
        setParams(Context.euc10);
        initialise(size);
    }

    public TestContext(Context c, int datasize) throws Exception {
        setParams(c);
        initialise(datasize);
    }

    public TestContext(Context c) throws Exception {
        setParams(c);
        initialise(1000 * 1000);
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return this.context;
    }

    protected void initialise(int datasize) throws Exception {

        switch (context) {
            case euc10: {
                tl = new TestLoad(10, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 10, 1));
            }
            break;

            case euc20: {
                tl = new TestLoad(20, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 20, 1));
            }
            break;

            case euc30: {
                tl = new TestLoad(30, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 30, 1));
            }
            break;

            case euc100: {
                tl = new TestLoad(100, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 100, 1));
            }
            break;

            case euc1000: {
                tl = new TestLoad(1000, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 1000, 1));
            }
            break;

            case euc10000: {
                tl = new TestLoad(10000, datasize, true, true);
                setThreshold(CartesianThresholds.getThreshold("euc", 10000, 1));
            }
            break;

            case nasa: {
                tl = new TestLoad(TestLoad.SisapFile.nasa);
                setThreshold(TestLoad.getSisapThresholds(TestLoad.SisapFile.nasa)[0]);
            }
            break;

            case colors: {
                tl = new TestLoad(TestLoad.SisapFile.colors);
                setThreshold(TestLoad.getSisapThresholds(TestLoad.SisapFile.colors)[0]);
            }
            break;

            default: {
                throw new Exception("unexpected test data specified");
            }
        }
    }

    protected void setParams(Context c) {
        context = c;
        measure = new Euclidean();
    }

    public List<CartesianPoint> getData() {
        return tl.getDataCopy();
    }

    public List<CartesianPoint> getDataCopy() {
        return tl.getDataCopy();
    }

    public List<CartesianPoint> getRefPoints() {
        return refPoints;
    }

    public List<CartesianPoint> getQueries() {
        return queries;
    }

    public void setSizes(int queries, int refPoints) {
        this.queries = tl.getQueries(queries);
        this.refPoints = tl.getQueries(refPoints);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double[] getThresholds() {
        switch (context) {
            case colors: {
                return TestLoad.getSisapThresholds(TestLoad.SisapFile.colors);
            }
            case nasa: {
                return TestLoad.getSisapThresholds(TestLoad.SisapFile.nasa);
            }
            case euc10: {
                double[] t = new double[3];
                t[0] = CartesianThresholds.getThreshold("euc", 10, 1);
                t[1] = CartesianThresholds.getThreshold("euc", 10, 2);
                t[2] = CartesianThresholds.getThreshold("euc", 10, 4);
                return t;
            }
            case euc20: {
                double[] t = new double[3];
                t[0] = CartesianThresholds.getThreshold("euc", 20, 1);
                t[1] = CartesianThresholds.getThreshold("euc", 20, 2);
                t[2] = CartesianThresholds.getThreshold("euc", 20, 4);
                return t;
            }
            default: {
                throw new RuntimeException("not implemented in TestContext");
            }
        }
    }

    public Measure<CartesianPoint> metric() {
        return measure;
    }

    public int dataSize() {
        return tl.dataSize();
    }

    /**
     * different types of data; SISAP colors and nasa files, various dimensions
     * of generated Euclidean spaces
     *
     * @author Richard Connor
     */
    public enum Context {
        colors, nasa, euc10, euc20, euc30, euc100, euc1000, euc10000
    }
}
