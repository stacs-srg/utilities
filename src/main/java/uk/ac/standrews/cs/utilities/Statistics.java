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
package uk.ac.standrews.cs.utilities;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.List;

/**
 * Class to calculate confidence intervals. It probably assumes that the data is normally distributed...
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class Statistics {

    /**
     * The default confidence level.
     */
    public static final double DEFAULT_CONFIDENCE_LEVEL = 0.95;

    public static double mean(final List<Double> values) {

        double total = 0.0;

        for (final double d : values) {
            total += d;
        }

        return total / values.size();
    }

    public static double standardDeviation(final List<Double> values) {

        final double[] array = new double[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return new StandardDeviation().evaluate(array);
    }

    /**
     * Calculates the confidence interval for a list of values, using the default confidence level.
     *
     * @param values the values
     * @return half the range of the confidence interval, such that the confidence interval is the mean plus/minus this value
     */
    public static double confidenceInterval(final List<Double> values) {

        return confidenceInterval(values, DEFAULT_CONFIDENCE_LEVEL);
    }

    /**
     * Calculates the confidence interval for a list of values.
     *
     * @param values the values
     * @param confidence_level the desired confidence level, i.e. the probability that the real mean lies within the confidence interval.
     * @return half the range of the confidence interval, such that the confidence interval is the mean plus/minus this value
     */
    public static double confidenceInterval(final List<Double> values, final double confidence_level) {

        return standardError(values) * criticalValue(sampleSize(values), confidence_level);
    }

    public static double standardError(final List<Double> values) {

        return standardDeviation(values) / Math.sqrt(sampleSize(values));
    }

    public static double criticalValue(final int number_of_values, final double confidence_level) {

        final int degrees_of_freedom = number_of_values - 1;
        return new TDistribution(degrees_of_freedom).inverseCumulativeProbability(oneTailedConfidenceLevel(confidence_level));
    }

    public static double oneTailedConfidenceLevel(final double confidence_level) {

        return 1 - (1 - confidence_level) / 2;
    }

    private static int sampleSize(final List<Double> values) {

        return values.size();
    }
}
