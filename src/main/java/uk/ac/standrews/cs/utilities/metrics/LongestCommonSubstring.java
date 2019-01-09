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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

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


public final class LongestCommonSubstring implements NamedMetric<String> {

    public LongestCommonSubstring() {
    }

    public double compare(String a, String b) {
        if( a == null || b == null ) {
            return 1.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 1.0;
        }
        else {
            return (float) lcs(a, b) / (float) Math.max(a.length(), b.length());
        }
    }

    public double distance(String a, String b) {
        if( a.equals(b) ) {
            return 0.0;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        } else if (a.isEmpty() ) {
            return (double) b.length();
        } else {
            return b.isEmpty() ? (double) a.length() : (double) (a.length() + b.length() - 2 * lcs(a, b));
        }
    }

    private static int lcs(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[] v0 = new int[n];
        int[] v1 = new int[n];
        int z = 0;

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (a.charAt(i) != b.charAt(j)) {
                    v1[j] = 0;
                } else {
                    if (i != 0 && j != 0) {
                        v1[j] = v0[j - 1] + 1;
                    } else {
                        v1[j] = 1;
                    }

                    if (v1[j] > z) {
                        z = v1[j];
                    }
                }
            }

            int[] swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return z;
    }

    public String toString() {
        return "LongestCommonSubstring";
    }

    @Override
    public String getMetricName() {
        return "LongestCommonSubstring";
    }

    public static void main(String[] a) {
        LongestCommonSubstring lcs = new LongestCommonSubstring();

        System.out.println("LongestCommonSubstring:" );

        System.out.println("empty string/empty string: " + lcs.distance("", ""));
        System.out.println("empty string/cat: " + lcs.distance("", "cat"));
        System.out.println("cat/empty string: " + lcs.distance("cat", ""));
        System.out.println("cat/cat: " + lcs.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  lcs.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + lcs.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + lcs.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +lcs.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + lcs.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + lcs.distance( "n", "zoological" ) );
    }
}
