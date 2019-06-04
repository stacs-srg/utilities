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
package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;

import static uk.ac.standrews.cs.utilities.metrics.KullbackLeibler.kullbackLeiblerDivergence;

/**
 * Created by al on 06/09/2017.
 */
public class JensenShannonKullbackLeibler implements NamedMetric<String> {

    @Override
    public String getMetricName() {
        return "JensenShannonKullbackLeibler";
    }

    /**
     * @return the JensenShannonDistance
     * The square root of the Jensen-Shannon divergence is a metric
     */
    public double distance(String x, String y) {

        double check = NamedMetric.checkNullAndEmpty(x, y);
        if (check != -1) return check;

        return Math.sqrt(jensenShannonDivergence(x, y));
    }

    @Override
    public double normalisedDistance(String s1, String s2) {
        // TODO: Should we normalise?
        return Metric.normalise(distance(s1, s2));
    }

    /**
     * @return the JensenShannonDistance (from elsewhere):
     * The square root of the Jensen-Shannon divergence is a metric
     */
    public double JensenShannonDistance(double[] x, double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException(String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));

        return Math.sqrt(jensenShannonDivergence(x, y));
    }

    /**
     * @return the Jensen-Shannon divergence.
     * The Jensen-Shannon divergence is a symmetric and smoothed version of the Kullback-Leibler divergence
     */
    public double jensenShannonDivergence(double[] p1, double[] p2) {

        assert (p1.length == p2.length);
        double[] average = new double[p1.length];
        for (int i = 0; i < p1.length; i++) {
            average[i] += (p1[i] + p2[i]) / 2;      // average of the two arrays.
        }
        return (kullbackLeiblerDivergence(p1, average) + kullbackLeiblerDivergence(p2, average)) / 2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
    }

    public double jensenShannonDivergence(String p1, String p2) {

        double check = NamedMetric.checkNullAndEmpty(p1, p2);
        if (check != -1) return 1 - check;

        SparseDistro p1_distro = new SparseDistro(NamedMetric.topAndTail(p1));
        SparseDistro p2_distro = new SparseDistro(NamedMetric.topAndTail(p2));

        if (p1.equals(p2)) {   // can have same sparseDistro for differering strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 1;
        }

        SparseDistro average = average(p1_distro, p2_distro);

        p1_distro.convertToProbabilityBased();
        p2_distro.convertToProbabilityBased();
        average.convertToProbabilityBased();

        double kl1 = kullbackLeiblerDivergence(p1_distro, average);
        double kl2 = kullbackLeiblerDivergence(p2_distro, average);

        return (kl1 + kl2) / 2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
    }

    private static SparseDistro average(SparseDistro xx, SparseDistro yy) {

        SparseDistro ave = new SparseDistro(xx); // a copy of the first distribution.
        // now average_value in the records from the second
        Iterator<QgramDistribution> yy_iter = yy.getIterator();
        while (yy_iter.hasNext()) {
            QgramDistribution nxt = yy_iter.next();
            ave.averageValue(nxt.key, nxt.count);
        }
        return ave;
    }

    public static void main(String[] args) {

        NamedMetric.printExamples(new JensenShannonKullbackLeibler());
    }
}
