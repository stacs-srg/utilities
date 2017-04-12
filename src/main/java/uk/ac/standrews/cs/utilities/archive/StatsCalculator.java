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
