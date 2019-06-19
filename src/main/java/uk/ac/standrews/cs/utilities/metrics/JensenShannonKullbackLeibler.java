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
package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.ac.standrews.cs.utilities.metrics.implementation.QgramDistribution;
import uk.ac.standrews.cs.utilities.metrics.implementation.SparseDistro;

import java.util.Iterator;

/**
 * Created by al on 06/09/2017.
 */
public class JensenShannonKullbackLeibler extends StringMetric {

    @Override
    public String getMetricName() {
        return "JensenShannonKullbackLeibler";
    }

    /**
     * @return the JensenShannonDistance
     * The square root of the Jensen-Shannon divergence is a metric
     */
    public double calculateStringDistance(String x, String y) {

        return normaliseArbitraryPositiveDistance(Math.sqrt(jensenShannonDivergence(x, y)));
    }

    private double jensenShannonDivergence(String p1, String p2) {

        SparseDistro p1_distro = new SparseDistro(topAndTail(p1));
        SparseDistro p2_distro = new SparseDistro(topAndTail(p2));

        if (p1_distro.equals(p2_distro)) {   // can have same sparseDistro for differering strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 0.0;
        }

        SparseDistro average = average(p1_distro, p2_distro);

        p1_distro.convertToProbabilityBased();
        p2_distro.convertToProbabilityBased();
        average.convertToProbabilityBased();

        double kl1 = kullbackLeiblerDivergence(p1_distro, average);
        double kl2 = kullbackLeiblerDivergence(p2_distro, average);

        return (kl1 + kl2) / 2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
    }

    /**
     * Implements the kullbackLeiblerDivergence over sparse representations.
     *
     * @return the Kullback–Leibler divergence
     */
    private static double kullbackLeiblerDivergence(SparseDistro distro_p, SparseDistro distro_q) {

        if (distro_p.equals(distro_q)) { // can have sane sparseDistro for differing strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 1;
        }

        Iterator<QgramDistribution> p_iter = distro_p.getIterator();

        double divergence = 0.0;

        while (p_iter.hasNext()) {

            QgramDistribution pi = p_iter.next();
            QgramDistribution qi = distro_q.getEntry(pi.key);

            if (qi == null) {
                // no coresponding bigram in q
                return Double.POSITIVE_INFINITY;
            }
            // keys are the same so do comparison
            divergence += pi.count * log2(pi.count / qi.count); //**** This is wrong!   // is it ? Math.log (natural).
        }
        return divergence;
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

    private static double log2 = Math.log(2);

    private static double log2(double x) {
        return Math.log(x) / log2;
    }

    public static void main(String[] args) {

        new JensenShannonKullbackLeibler().printExamples();
    }
}
