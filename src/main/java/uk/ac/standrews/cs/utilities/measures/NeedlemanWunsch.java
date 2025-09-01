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

import org.simmetrics.metrics.functions.MatchMismatch;
import org.simmetrics.metrics.functions.Substitution;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

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
public class NeedlemanWunsch extends StringMeasure {

    private static final Substitution DEFAULT_SUBSTITUTION = new MatchMismatch(0.0F, -1.0F);
    private static final float DEFAULT_GAP_VALUE = -2.0F;

    private final Substitution substitution;
    private final double gapValue;

    public NeedlemanWunsch() {
        this(DEFAULT_GAP_VALUE, DEFAULT_SUBSTITUTION);
    }

    public NeedlemanWunsch(final double gapValue, final Substitution substitution) {

        this.gapValue = gapValue;
        this.substitution = substitution;
    }

    @Override
    public String getMeasureName() {
        return "NeedlemanWunsch";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {
        return 1.0 - similarity(clean(x), clean(y));
    }

    private double similarity(final String x, final String y) {

        final double maxDistance = (double) Math.max(x.length(), y.length()) * Math.max(substitution.max(), gapValue);
        final double minDistance = (double) Math.max(x.length(), y.length()) * Math.min(substitution.min(), gapValue);

        return (-needlemanWunsch(x, y) - minDistance) / (maxDistance - minDistance);
    }

    private double needlemanWunsch(final String x, final String y) {

        final int lengthX = x.length();
        final int lengthY = y.length();

        double[] v0 = new double[lengthY + 1];
        double[] v1 = new double[lengthY + 1];

        for (int i = 0; i <= lengthY; ++i) {
            v0[i] = i;
        }

        for (int i = 1; i <= lengthX; ++i) {
            v1[0] = i;

            for (int j = 1; j <= lengthY; ++j) {
                v1[j] = min(v0[j] - gapValue, v1[j - 1] - gapValue, v0[j - 1] - substitution.compare(x, i - 1, y, j - 1));
            }

            double[] swap = v0;
            v0 = v1;
            v1 = swap;
        }

        return v0[lengthY];
    }

    public String toString() {
        return getMeasureName() + " [costFunction=" + substitution + ", gapCost=" + gapValue + "]";
    }

    public static void main(String[] a) {

        new NeedlemanWunsch().printExamples();
    }
}
