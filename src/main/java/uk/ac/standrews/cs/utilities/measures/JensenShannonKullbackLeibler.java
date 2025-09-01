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
package uk.ac.standrews.cs.utilities.measures;

import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;
import uk.ac.standrews.cs.utilities.measures.implementation.QgramDistribution;
import uk.ac.standrews.cs.utilities.measures.implementation.SparseDistribution;

/**
 * The square root of the Jensen-Shannon divergence is a metric
 */
public class JensenShannonKullbackLeibler extends StringMeasure {

    private static final double LOG_OF_2 = Math.log(2);

    @Override
    public String getMeasureName() {
        return "JensenShannonKullbackLeibler";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        return Math.sqrt(jensenShannonDivergence(clean(x), clean(y)));
    }

    private static double jensenShannonDivergence(final String x, final String y) {

        final SparseDistribution distributionX = new SparseDistribution(topAndTail(x));
        final SparseDistribution distributionY = new SparseDistribution(topAndTail(y));

        if (distributionX.equals(distributionY)) {   // can have same sparse distribution for differering strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 0.0;
        }

        final SparseDistribution average = average(distributionX, distributionY);

        distributionX.convertToProbabilityBased();
        distributionY.convertToProbabilityBased();
        average.convertToProbabilityBased();

        final double divergenceX = kullbackLeiblerDivergence(distributionX, average);
        final double divergenceY = kullbackLeiblerDivergence(distributionY, average);

        return (divergenceX + divergenceY) / 2;   // according to Richard (SISAP_2013_JS.pdf) each term should be halved
    }

    /**
     * Implements the kullbackLeiblerDivergence over sparse representations.
     *
     * @return the Kullback–Leibler divergence
     */
    private static double kullbackLeiblerDivergence(final SparseDistribution x, final SparseDistribution y) {

        if (x.equals(y)) { // can have same sparse distribution for differing strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 1.0;
        }

//        Iterator<QgramDistribution> p_iter = x.getIterator();

        double divergence = 0.0;

        for (final QgramDistribution qGramDistributionX : x) {

            final QgramDistribution qGramDistributionY = y.getEntry(qGramDistributionX.key);

            if (qGramDistributionY == null) {
                // no corresponding bigram in q
                return Double.POSITIVE_INFINITY;
            }

            // keys are the same so do comparison
            divergence += qGramDistributionX.count * log2(qGramDistributionX.count / qGramDistributionY.count);
        }

//        while (p_iter.hasNext()) {
//
//            QgramDistribution pi = p_iter.next();
//            QgramDistribution qi = y.getEntry(pi.key);
//
//            if (qi == null) {
//                // no coresponding bigram in q
//                return Double.POSITIVE_INFINITY;
//            }
//            // keys are the same so do comparison
//            divergence += pi.count * log2(pi.count / qi.count); //**** This is wrong!   // is it ? Math.log (natural).
//        }
        return divergence;
    }

    private static SparseDistribution average(final SparseDistribution x, final SparseDistribution y) {

        final SparseDistribution average = new SparseDistribution(x); // a copy of the first distribution.
        // now average_value in the records from the second

        for (final QgramDistribution qGramDistributionY : y) {
            average.averageValue(qGramDistributionY.key, qGramDistributionY.count);
        }
        return average;
    }

    private static double log2(final double x) {
        return Math.log(x) / LOG_OF_2;
    }

    public static void main(String[] args) {

        new JensenShannonKullbackLeibler().printExamples();
    }
}
