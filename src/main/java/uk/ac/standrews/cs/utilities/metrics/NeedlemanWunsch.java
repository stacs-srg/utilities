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
package uk.ac.standrews.cs.utilities.metrics;

import com.google.common.base.Preconditions;
import org.simmetrics.metrics.functions.MatchMismatch;
import org.simmetrics.metrics.functions.Substitution;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide float based similarity
 * measures between String Data. All metrics return consistant measures
 * rather than unbounded similarity scores.
 * <p>
 * Copyright (C) 2005 Sam Chapman - Open Source Release v1.1
 * <p>
 * Please Feel free to contact me about this library, I would appreciate
 * knowing quickly what you wish to use it for and any criticisms/comments
 * upon the SimMetric library.
 * <p>
 * email:       s.chapman@dcs.shef.ac.uk
 * www:         http://www.dcs.shef.ac.uk/~sam/
 * www:         http://www.dcs.shef.ac.uk/~sam/stringmetrics.html
 * <p>
 * address:     Sam Chapman,
 * Department of Computer Science,
 * University of Sheffield,
 * Sheffield,
 * S. Yorks,
 * S1 4DP
 * United Kingdom,
 * <p>
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Code included for speed tests - modified to comply with our interfaces.
 */

public class NeedlemanWunsch extends StringMetric {

    private static final Substitution MATCH_0_MISMATCH_1 = new MatchMismatch(0.0F, -1.0F);
    private final Substitution substitution;
    private final double gapValue;

    public NeedlemanWunsch() {
        this(-2.0F, MATCH_0_MISMATCH_1);
    }

    public NeedlemanWunsch(double gapValue, Substitution substitution) {

        Preconditions.checkArgument(gapValue <= 0.0F);
        Preconditions.checkNotNull(substitution);
        this.gapValue = gapValue;
        this.substitution = substitution;
    }

    static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    @Override
    public String getMetricName() {
        return "NeedlemanWunsch";
    }

    public double calculateStringDistance(String a, String b) {
        return 1.0 - similarity(a, b);
    }

    private double similarity(String a, String b) {

        double maxDistance = (double) Math.max(a.length(), b.length()) * Math.max(substitution.max(), gapValue);
        double minDistance = (double) Math.max(a.length(), b.length()) * Math.min(substitution.min(), gapValue);

        return (-needlemanWunsch(a, b) - minDistance) / (maxDistance - minDistance);
    }

    private double needlemanWunsch(String s, String t) {

        int n = s.length();
        int m = t.length();
        double[] v0 = new double[m + 1];
        double[] v1 = new double[m + 1];

        int i;
        for (i = 0; i <= m; ++i) {
            v0[i] = (double) i;
        }

        for (i = 1; i <= n; ++i) {
            v1[0] = (double) i;

            for (int j = 1; j <= m; ++j) {
                v1[j] = min(v0[j] - gapValue, v1[j - 1] - gapValue, v0[j - 1] - substitution.compare(s, i - 1, t, j - 1));
            }

            double[] swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return v0[m];
    }

    public String toString() {
        return "NeedlemanWunsch [costFunction=" + substitution + ", gapCost=" + gapValue + "]";
    }

    public static void main(String[] a) {

        new NeedlemanWunsch().printExamples();
    }
}
