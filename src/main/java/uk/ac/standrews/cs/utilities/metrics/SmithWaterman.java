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

import com.google.common.base.Preconditions;
import org.simmetrics.metrics.functions.AffineGap;
import org.simmetrics.metrics.functions.Gap;
import org.simmetrics.metrics.functions.MatchMismatch;
import org.simmetrics.metrics.functions.Substitution;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide double based similarity
 * measures between String Data. All metrics return consistant measures
 * rather than unbounded similarity scores.
 *
 * Copyright (C) 2005 Sam Chapman - Open Source Release v1.1
 *
 * Please Feel free to contact me about this library, I would appreciate
 * knowing quickly what you wish to use it for and any criticisms/comments
 * upon the SimMetric library.
 *
 * email:       s.chapman@dcs.shef.ac.uk
 * www:         http://www.dcs.shef.ac.uk/~sam/
 * www:         http://www.dcs.shef.ac.uk/~sam/stringmetrics.html
 *
 * address:     Sam Chapman,
 *              Department of Computer Science,
 *              University of Sheffield,
 *              Sheffield,
 *              S. Yorks,
 *              S1 4DP
 *              United Kingdom,
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 *  Code included for speed tests - modified to comply with our interfaces.
 */


public class SmithWaterman  implements NamedMetric<String> {
    private final Gap gap;
    private final Substitution substitution;
    private final int windowSize;

    public SmithWaterman() {
        this(new AffineGap(-5.0F, -1.0F), new MatchMismatch(5.0F, -3.0F), 2147483647);
    }

    public SmithWaterman(Gap gap, Substitution substitution, int windowSize) {
        Preconditions.checkNotNull(gap);
        Preconditions.checkNotNull(substitution);
        Preconditions.checkArgument(windowSize >= 0);
        this.gap = gap;
        this.substitution = substitution;
        this.windowSize = windowSize;
    }

    public double distance(String a, String b) {
        return 1.0 - this.compare(a, b);
    }

    public double compare(String a, String b) {

        double check = CheckValues.checkNullAndEmpty(a, b);
        if (check != -1) return check;

        double maxDistance = (double)Math.min(a.length(), b.length()) * Math.max(this.substitution.max(), this.gap.min());
        return this.smithWaterman(a, b) / maxDistance;
    }

    private double smithWaterman(String a, String b) {
        int n = a.length();
        int m = b.length();
        double[][] d = new double[n][m];
        double max = d[0][0] = Math.max(0.0, this.substitution.compare(a, 0, b, 0));

        int i;
        double maxGapCost;
        int k;
        for(i = 0; i < n; ++i) {
            maxGapCost = 0.0;

            for(k = Math.max(1, i - this.windowSize); k < i; ++k) {
                maxGapCost = Math.max(maxGapCost, d[i - k][0] + this.gap.value(i - k, i));
            }

            d[i][0] = max(0.0, maxGapCost, this.substitution.compare(a, i, b, 0));
            max = Math.max(max, d[i][0]);
        }

        for(i = 1; i < m; ++i) {
            maxGapCost = 0.0;

            for(k = Math.max(1, i - this.windowSize); k < i; ++k) {
                maxGapCost = Math.max(maxGapCost, d[0][i - k] + this.gap.value(i - k, i));
            }

            d[0][i] = max(0.0, maxGapCost, this.substitution.compare(a, 0, b, i));
            max = Math.max(max, d[0][i]);
        }

        for(i = 1; i < n; ++i) {
            for(int j = 1; j < m; ++j) {
                maxGapCost = 0.0;

                for(k = Math.max(1, i - this.windowSize); k < i; ++k) {
                    maxGapCost = Math.max(maxGapCost, d[i - k][j] + this.gap.value(i - k, i));
                }

                for(k = Math.max(1, j - this.windowSize); k < j; ++k) {
                    maxGapCost = Math.max(maxGapCost, d[i][j - k] + this.gap.value(j - k, j));
                }

                d[i][j] = max(0.0, maxGapCost, d[i - 1][j - 1] + this.substitution.compare(a, i, b, j));
                max = Math.max(max, d[i][j]);
            }
        }

        return max;
    }

    static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    public String toString() {
        return "SmithWaterman [gap=" + this.gap + ", substitution=" + this.substitution + ", windowSize=" + this.windowSize + "]";
    }

    @Override
    public String getMetricName() {
        return "SmithWaterman";
    }

    public static void main(String[] a) {
        SmithWaterman sw = new SmithWaterman();

        System.out.println("SmithWaterman:" );

        System.out.println("empty string/empty string: " + sw.distance("", ""));
        System.out.println("empty string/cat: " + sw.distance("", "cat"));
        System.out.println("cat/empty string: " + sw.distance("cat", ""));
        System.out.println("cat/cat: " + sw.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  sw.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + sw.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + sw.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +sw.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + sw.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + sw.distance( "n", "zoological" ) );
    }
}
