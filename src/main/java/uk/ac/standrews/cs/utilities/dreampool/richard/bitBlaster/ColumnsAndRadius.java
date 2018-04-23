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
package uk.ac.standrews.cs.utilities.dreampool.richard.bitBlaster;

import uk.ac.standrews.cs.utilities.dreampool.CountingWrapper;
import uk.ac.standrews.cs.utilities.dreampool.Query;
import uk.ac.standrews.cs.utilities.dreampool.richard.coreConcepts.CountedMetric;
import uk.ac.standrews.cs.utilities.dreampool.richard.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.dreampool.richard.dataPoints.cartesian.Euclidean;
import uk.ac.standrews.cs.utilities.dreampool.richard.searchStructures.VPTree;
import uk.ac.standrews.cs.utilities.dreampool.richard.testloads.TestContext;
import uk.ac.standrews.cs.utilities.metrics.CartesianPoint;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;



public class ColumnsAndRadius {

    private static final double RADIUS_INCREMENT = 0.1;
    private static double meanDist = 0.418;
    private static boolean fourPoint = true;

    public static void main(String[] args) throws Exception {

        int noOfRefPoints = 28;
        final int nChoose2 = ((noOfRefPoints - 1) * noOfRefPoints) / 2;
        final int noOfBitSets = nChoose2 + noOfRefPoints * 3;

        final TestContext.Context context = TestContext.Context.colors;
        TestContext tc = new TestContext(context);
        tc.setSizes(tc.dataSize() / 10, noOfRefPoints);
        List<CartesianPoint> dat = tc.getData();
        List<CartesianPoint> refPool = tc.getRefPoints();
        List<CartesianPoint> refs = refPool;
        double t = tc.getThreshold();

        List<CartesianPoint> queries = tc.getQueries();
        System.out.println("context\t" + context);
        System.out.println("data size\t" + dat.size());
        System.out.println("query size\t" + queries.size());
        System.out.println("threshold\t" + t);
        System.out.println("refs size\t" + refs.size());
        System.out.println("no of bitsets\t" + noOfBitSets);

        BitSet[] datarep = new BitSet[noOfBitSets];

        buildBitSetData(noOfRefPoints, nChoose2, noOfBitSets, tc, dat, refs,
                datarep);

        queryBitSetData(noOfRefPoints, nChoose2, tc, dat, refs, t, queries,
                datarep);

        buildAndQueryVpt(noOfRefPoints, tc, dat, refs, datarep);
    }

    private static void buildAndQueryVpt(int noOfRefPoints, TestContext tc,
                                         List<CartesianPoint> dat, List<CartesianPoint> refs,
                                         BitSet[] datarep) {

        dat.addAll(tc.getRefPoints());

        CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
        VPTree<CartesianPoint> vpt = new VPTree<>(dat, cm);
        cm.reset();
        final List<CartesianPoint> queries = tc.getQueries();
        final double t = tc.getThreshold();

        long t0 = System.currentTimeMillis();
        int noOfRes = 0;
        for (CartesianPoint q : queries) {
            List<CartesianPoint> res = vpt.thresholdSearch(q, t);
            noOfRes += res.size();
        }

        System.out.println("vpt");
        System.out.println("dists per query\t" + cm.reset() / queries.size());
        System.out.println("results\t" + noOfRes);
        System.out.println("time\t" + (System.currentTimeMillis() - t0)
                / (float) queries.size());
    }

    private static void buildBitSetData(int noOfRefPoints, final int nChoose2,
                                        final int noOfBitSets, TestContext tc, List<CartesianPoint> dat,
                                        List<CartesianPoint> refs, BitSet[] datarep) {
        final int dataSize = dat.size();

        for (int i = 0; i < noOfBitSets; i++) {
            datarep[i] = new BitSet(dataSize);
        }

        for (int n = 0; n < dataSize; n++) {
            CartesianPoint p = dat.get(n);
            double[] refDists = new double[noOfRefPoints];
            for (int i = 0; i < noOfRefPoints; i++) {
                refDists[i] = tc.metric().distance(p, refs.get(i));
            }

            int ptr = 0;
            for (int i = 0; i < noOfRefPoints - 1; i++) {
                final double d1 = refDists[i];
                for (int j = i + 1; j < noOfRefPoints; j++) {
                    final double d2 = refDists[j];
                    boolean leftCloser = d1 < d2;
                    datarep[ptr].set(n, leftCloser);
                    ptr++;
                }
            }
            // and do radius inclusion now
            for (int i = 0; i < noOfRefPoints; i++) {
                boolean insideMean = refDists[i] <= meanDist;
                boolean insideBigger = refDists[i] <= meanDist
                        + RADIUS_INCREMENT;
                boolean insideSmaller = refDists[i] <= meanDist
                        - RADIUS_INCREMENT;
                datarep[nChoose2 + i].set(n, insideMean);
                // hacky... find somewhere unique to store these!
                datarep[nChoose2 + i + noOfRefPoints].set(n, insideSmaller);
                datarep[nChoose2 + i + noOfRefPoints * 2].set(n, insideBigger);
            }
        }
    }

    private static void doExclusions(List<CartesianPoint> dat, double t,
                                     BitSet[] datarep, CountedMetric<CartesianPoint> cm,
                                     CartesianPoint q, List<CartesianPoint> res, final int dataSize,
                                     List<Integer> mustBeIn, List<Integer> cantBeIn) {
        if (mustBeIn.size() != 0) {
            BitSet ands = getAndBitSets(datarep, dataSize, mustBeIn);
            if (cantBeIn.size() != 0) {
                /*
                 * hopefully the normal situation or we're in trouble!
                 */
                BitSet nots = getOrBitSets(datarep, dataSize, cantBeIn);
                nots.flip(0, dataSize);
                ands.and(nots);
                filterContendors(dat, t, cm, q, res, dataSize, ands);
            } else {
                // there are no cantBeIn partitions
                filterContendors(dat, t, cm, q, res, dataSize, ands);
            }
        } else {
            // there are no mustBeIn partitions
            if (cantBeIn.size() != 0) {
                BitSet nots = getOrBitSets(datarep, dataSize, cantBeIn);
                nots.flip(0, dataSize);
                filterContendors(dat, t, cm, q, res, dataSize, nots);
            } else {
                // there are no exclusions at all...
                for (CartesianPoint d : dat) {
                    if (cm.distance(q, d) < t) {
                        res.add(d);
                    }
                }
            }
        }
    }

    @SuppressWarnings("boxing")
    private static void excludeHyperplanePartitions(int noOfRefPoints,
                                                    double t, double[][] refDists, double[] queryDists,
                                                    List<Integer> mustBeIn, List<Integer> cantBeIn) {
        int partitionPointer = 0;
        for (int i = 0; i < noOfRefPoints - 1; i++) {
            double d1 = queryDists[i];
            for (int j = i + 1; j < noOfRefPoints; j++) {
                double d2 = queryDists[j];

                boolean cond1 = false;
                if (fourPoint) {
                    cond1 = (d2 * d2 - d1 * d1) / refDists[i][j] > 2 * t;
                } else {
                    cond1 = (d2 - d1) > 2 * t;
                }
                if (cond1) {
                    mustBeIn.add(partitionPointer);
                } else {
                    boolean cond2 = false;
                    if (fourPoint) {
                        cond2 = (d1 * d1 - d2 * d2) / refDists[i][j] > 2 * t;
                    } else {
                        cond2 = (d1 - d2) > 2 * t; // Richard had cond1 <<< TODO WRONG?
                    }
                    if (cond2) {
                        cantBeIn.add(partitionPointer);  // TODO is this wrong????
                    }
                }
                partitionPointer++;
            }
        }
    }

    @SuppressWarnings("boxing")
    private static void excludeRadiusPartitions(int noOfRefPoints,
                                                final int nChoose2, double t, double[] queryDists,
                                                List<Integer> mustBeIn, List<Integer> cantBeIn) {
        for (int i = 0; i < noOfRefPoints; i++) {
            if (queryDists[i] < meanDist - RADIUS_INCREMENT - t) {
                mustBeIn.add(nChoose2 + i + noOfRefPoints);
            } else if (queryDists[i] < meanDist - t) {
                mustBeIn.add(nChoose2 + i);
            } else if (queryDists[i] < meanDist + RADIUS_INCREMENT - t) {
                mustBeIn.add(nChoose2 + i + noOfRefPoints * 2);
            }

            if (queryDists[i] >= t + meanDist + RADIUS_INCREMENT) {
                cantBeIn.add(nChoose2 + i + noOfRefPoints * 2);
            } else if (queryDists[i] >= t + meanDist) {
                cantBeIn.add(nChoose2 + i);
            } else if (queryDists[i] >= t + meanDist - RADIUS_INCREMENT) {
                cantBeIn.add(nChoose2 + i + noOfRefPoints);
            }
        }
    }

    private static void filterContendors(List<CartesianPoint> dat, double t,
                                         CountedMetric<CartesianPoint> cm, CartesianPoint q,
                                         List<CartesianPoint> res, final int dataSize, BitSet ands) {
        for (int i = 0; i < dataSize; i++) {
            if (ands.get(i)) {
                if (cm.distance(q, dat.get(i)) < t) {
                    res.add(dat.get(i));
                }
            }
        }
    }

    @SuppressWarnings("boxing")
    private static BitSet getAndBitSets(BitSet[] datarep, final int dataSize,
                                        List<Integer> mustBeIn) {
        BitSet ands = null;
        if (mustBeIn.size() != 0) {
            ands = datarep[mustBeIn.get(0)].get(0, dataSize);
            for (int i = 1; i < Math.min(1000, mustBeIn.size()); i++) {   // TODO <<<<<<< What is 1000 about?
                ands.and(datarep[mustBeIn.get(i)]);
            }
        }
        return ands;
    }

    @SuppressWarnings("boxing")
    private static BitSet getOrBitSets(BitSet[] datarep, final int dataSize,
                                       List<Integer> cantBeIn) {
        BitSet nots = null;
        if (cantBeIn.size() != 0) {
            nots = datarep[cantBeIn.get(0)].get(0, dataSize);
            for (int i = 1; i < Math.min(1000, cantBeIn.size()); i++) {
                final BitSet nextNot = datarep[cantBeIn.get(i)];
                nots.or(nextNot);
            }
        }
        return nots;
    }

    private static <T> double[] getQueryToRefDists(List<T> refs, double t,
                                                   CountedMetric<T> cm, T q, List<T> res) {
        /*
         * first get all the query to ref point distances, remembering to add
         * them to the result set
         */
        double[] queryDists = new double[refs.size()];
        for (int i = 0; i < refs.size(); i++) {
            final T thisRef = refs.get(i);
            final double d = cm.distance(thisRef, q);
            queryDists[i] = d;
            if (d < t) {
                res.add(thisRef);
            }
        }
        return queryDists;
    }

    private static <T> double[][] getRefDistArray(int noOfRefPoints,
                                                  Metric<T> metric, List<T> refs) {
        double[][] refDists = new double[noOfRefPoints][noOfRefPoints];
        for (int i = 0; i < noOfRefPoints - 1; i++) {
            for (int j = i + 1; j < noOfRefPoints; j++) {
                final double d = metric.distance(refs.get(i), refs.get(j));
                refDists[i][j] = d;
            }
        }
        return refDists;
    }

    private static void queryBitSetData(int noOfRefPoints, final int nChoose2,
                                        TestContext tc, List<CartesianPoint> dat,
                                        List<CartesianPoint> refs, double t, List<CartesianPoint> queries,
                                        BitSet[] datarep) {

        double[][] refDists = getRefDistArray(noOfRefPoints, tc.metric(), refs);

        int noOfResults = 0;
        int partitionsExcluded = 0;
        long t0 = System.currentTimeMillis();

        CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
        for (CartesianPoint q : queries) {
            List<CartesianPoint> res = new ArrayList<>();
            double[] queryDists = getQueryToRefDists(refs, t, cm, q, res);
            final int dataSize = dat.size();

            /*
             * lists of partition ids where any solution to q (a) must, and (b)
             * can't, be an element of
             */
            List<Integer> mustBeIn = new ArrayList<>();
            List<Integer> cantBeIn = new ArrayList<>();

            excludeHyperplanePartitions(noOfRefPoints, t, refDists, queryDists,
                    mustBeIn, cantBeIn);

            partitionsExcluded += cantBeIn.size() + mustBeIn.size();

            excludeRadiusPartitions(noOfRefPoints, nChoose2, t, queryDists,
                    mustBeIn, cantBeIn);

            doExclusions(dat, t, datarep, cm, q, res, dataSize, mustBeIn,
                    cantBeIn);

            noOfResults += res.size();
            HashSet<CartesianPoint> set = new HashSet<CartesianPoint>(res);
            Query<CartesianPoint> Q = new Query<CartesianPoint>( q, null, (float) t, dat, null, new CountingWrapper<CartesianPoint>( new Euclidean<>() ), false );
            Q.checkSolutions(set);

        }
        System.out.println("bitsets");
        System.out.println("dists per query\t" + cm.reset() / queries.size());
        System.out.println("results\t" + noOfResults);
        System.out.println("time\t" + (System.currentTimeMillis() - t0)
                / (float) queries.size());
        System.out.println("partitions excluded\t"
                + ((double) partitionsExcluded / queries.size()) / nChoose2);

    }

}