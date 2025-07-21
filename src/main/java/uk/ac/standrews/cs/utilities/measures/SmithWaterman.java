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

import org.simmetrics.metrics.functions.AffineGap;
import org.simmetrics.metrics.functions.Gap;
import org.simmetrics.metrics.functions.MatchMismatch;
import org.simmetrics.metrics.functions.Substitution;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide double based similarity
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
 * with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * <p>
 * Code included for speed tests - modified to comply with our interfaces.
 */
public class SmithWaterman extends StringMeasure {

    private static final AffineGap DEFAULT_GAP = new AffineGap(-5.0F, -1.0F);
    private static final MatchMismatch DEFAULT_SUBSTITUTION = new MatchMismatch(5.0F, -3.0F);
    private static final int DEFAULT_WINDOW_SIZE = 2147483647;

    private final Gap gap;
    private final Substitution substitution;
    private final int windowSize;

    public SmithWaterman() {
        this(DEFAULT_GAP, DEFAULT_SUBSTITUTION, DEFAULT_WINDOW_SIZE);
    }

    public SmithWaterman(final Gap gap, final Substitution substitution, final int windowSize) {

        this.gap = gap;
        this.substitution = substitution;
        this.windowSize = windowSize;
    }

    @Override
    public String getMeasureName() {
        return "SmithWaterman";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        return 1.0 - similarity(clean(x), clean(y));
    }

    private double similarity(final String x, final String y) {

        if (x.isEmpty() || y.isEmpty()) return 0.0;

        final double maxDistance = (double) Math.min(x.length(), y.length()) * Math.max(substitution.max(), gap.min());
        return smithWaterman(x, y) / maxDistance;
    }

    private double smithWaterman(final String x, final String y) {

        final int lengthX = x.length();
        final int lengthY = y.length();

        final double[][] d = new double[lengthX][lengthY];
        double max = d[0][0] = Math.max(0.0, substitution.compare(x, 0, y, 0));

        for (int i = 0; i < lengthX; ++i) {
            double maxGapCost = 0.0;

            for (int k = Math.max(1, i - windowSize); k < i; ++k) {
                maxGapCost = Math.max(maxGapCost, d[i - k][0] + gap.value(i - k, i));
            }

            d[i][0] = max(0.0, maxGapCost, substitution.compare(x, i, y, 0));
            max = Math.max(max, d[i][0]);
        }

        for (int i = 1; i < lengthY; ++i) {
            double maxGapCost = 0.0;

            for (int k = Math.max(1, i - windowSize); k < i; ++k) {
                maxGapCost = Math.max(maxGapCost, d[0][i - k] + gap.value(i - k, i));
            }

            d[0][i] = max(0.0, maxGapCost, substitution.compare(x, 0, y, i));
            max = Math.max(max, d[0][i]);
        }

        for (int i = 1; i < lengthX; ++i) {
            for (int j = 1; j < lengthY; ++j) {
                double maxGapCost = 0.0;

                for (int k = Math.max(1, i - windowSize); k < i; ++k) {
                    maxGapCost = Math.max(maxGapCost, d[i - k][j] + gap.value(i - k, i));
                }

                for (int k = Math.max(1, j - windowSize); k < j; ++k) {
                    maxGapCost = Math.max(maxGapCost, d[i][j - k] + gap.value(j - k, j));
                }

                d[i][j] = max(0.0, maxGapCost, d[i - 1][j - 1] + substitution.compare(x, i, y, j));
                max = Math.max(max, d[i][j]);
            }
        }

        return max;
    }

    public String toString() {
        return getMeasureName() + " [gap=" + this.gap + ", substitution=" + substitution + ", windowSize=" + windowSize + "]";
    }

    public static void main(String[] a) {

        new SmithWaterman().printExamples();
    }
}
