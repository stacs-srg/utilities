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
public final class LongestCommonSubstring extends StringMetric {

    @Override
    public String getMetricName() {
        return "LongestCommonSubstring";
    }

    public double calculateStringDistance(String a, String b) {

        return normaliseArbitraryPositiveDistance(a.length() + b.length() - 2 * lengthOfLongestCommonSubstring(a, b));
    }

    public String toString() {
        return "LongestCommonSubstring";
    }

    private int lengthOfLongestCommonSubstring(String a, String b) {

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

    public static void main(String[] a) {

        new LongestCommonSubstring().printExamples();
    }
}
