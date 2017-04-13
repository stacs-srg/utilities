/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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
/*
 * Created on 21-Jan-2005
 */
package uk.ac.standrews.cs.utilities.archive;

/**
 * @author stuart
 */
public class StatsCalculator {
    /**
     * Calculates the standard deviation of an array
     * of numbers.
     *
     * @param data Numbers to compute the standard deviation of.
     *             Array must contain two or more numbers.
     * @return standard deviation estimate of population
     * ( to get estimate of sample, use n instead of n-1 in last line )
     */
    public static double standardDeviation(double[] data) {
        // sd is sqrt of sum of (values-mean) squared divided by n - 1
        // Calculate the mean
        double mean = 0;
        final int n = data.length;
        if (data != null && n < 2)
            return Double.NaN;
        for (int i = 0; i < n; i++) {
            mean += data[i];
        }
        mean /= n;
        // calculate the sum of squares
        double sum = 0;
        for (int i = 0; i < n; i++) {
            final double v = data[i] - mean;
            sum += v * v;
        }
        return Math.sqrt(sum / (n - 1));
    }

    public static double mean(double[] data) {
        double result = Double.NaN;
        if (data != null && data.length > 0) {
            double sum = 0.0;
            for (int i = 0; i < data.length; i++) {
                sum += data[i];
            }
            result = sum / data.length;
        }
        return result;
    }

    public static double mean(int[] data) {
        double result = Double.NaN;
        if (data != null && data.length > 0) {
            double sum = 0.0;
            for (int i = 0; i < data.length; i++) {
                sum += data[i];
            }
            result = sum / data.length;
        }
        return result;
    }
}
