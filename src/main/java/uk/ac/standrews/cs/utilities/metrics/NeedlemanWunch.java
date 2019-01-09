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
import org.simmetrics.metrics.functions.MatchMismatch;
import org.simmetrics.metrics.functions.Substitution;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Objects;

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide float based similarity
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
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Code included for speed tests - modified to comply with our interfaces.
 */

public class NeedlemanWunch implements NamedMetric<String> {

    private static final Substitution MATCH_0_MISMATCH_1 = new MatchMismatch(0.0F, -1.0F);
    private final Substitution substitution;
    private final double gapValue;

    public NeedlemanWunch() {
        this(-2.0F, MATCH_0_MISMATCH_1);
    }

    public NeedlemanWunch(double gapValue, Substitution substitution) {
        Preconditions.checkArgument(gapValue <= 0.0F);
        Preconditions.checkNotNull(substitution);
        this.gapValue = gapValue;
        this.substitution = substitution;
    }

    public double distance(String a,String b) {
        return 1.0 - this.compare(a, b);
    }

    public double compare(String a, String b) {

        double check = CheckValues.checkNullAndEmpty(a, b);
        if (check != -1) return check;

        double maxDistance = (double)Math.max(a.length(), b.length()) * Math.max(this.substitution.max(), this.gapValue);
        double minDistance = (double)Math.max(a.length(), b.length()) * Math.min(this.substitution.min(), this.gapValue);
        return (-this.needlemanWunch(a, b) - minDistance) / (maxDistance - minDistance);
    }

    private double needlemanWunch(String s, String t) {
        if (Objects.equals(s, t)) {
            return 0.0;
        } else if (s.isEmpty()) {
            return -this.gapValue * (double)t.length();
        } else if (t.isEmpty()) {
            return -this.gapValue * (double)s.length();
        } else {
            int n = s.length();
            int m = t.length();
            double[] v0 = new double[m + 1];
            double[] v1 = new double[m + 1];

            int i;
            for(i = 0; i <= m; ++i) {
                v0[i] = (double)i;
            }

            for(i = 1; i <= n; ++i) {
                v1[0] = (double)i;

                for(int j = 1; j <= m; ++j) {
                    v1[j] = min(v0[j] - this.gapValue, v1[j - 1] - this.gapValue, v0[j - 1] - this.substitution.compare(s, i - 1, t, j - 1));
                }

                double[] swap = v0;
                v0 = v1;
                v1 = swap;
            }

            return v0[m];
        }
    }

    static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    public String toString() {
        return "NeedlemanWunch [costFunction=" + this.substitution + ", gapCost=" + this.gapValue + "]";
    }

    @Override
    public String getMetricName() {
        return "NeedlemanWunch";
    }

    public static void main(String[] a) {
        NeedlemanWunch nw = new NeedlemanWunch();

        System.out.println("NeedlemanWunch:" );

        System.out.println("empty string/empty string: " + nw.distance("", ""));
        System.out.println("empty string/cat: " + nw.distance("", "cat"));
        System.out.println("cat/empty string: " + nw.distance("cat", ""));
        System.out.println("cat/cat: " + nw.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  nw.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + nw.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + nw.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +nw.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + nw.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + nw.distance( "n", "zoological" ) );
    }
}
