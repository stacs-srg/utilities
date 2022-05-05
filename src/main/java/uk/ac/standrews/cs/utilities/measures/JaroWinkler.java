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

import com.google.common.base.Strings;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

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
 * with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 *  Code included for speed tests - modified to comply with our interfaces.
 */
public final class JaroWinkler extends StringMeasure {

    private final Jaro jaro = new Jaro();

    private static final double DEFAULT_BOOST_THRESHOLD = 0.0;
    private static final double DEFAULT_PREFIX_SCALE = 0.1F;
    private static final int DEFAULT_MAX_PREFIX_LENGTH = 4;

    private final double boostThreshold;
    private final double prefixScale;
    private final int maxPrefixLength;

    public JaroWinkler() {
        this(DEFAULT_BOOST_THRESHOLD, DEFAULT_PREFIX_SCALE, DEFAULT_MAX_PREFIX_LENGTH);
    }

    public JaroWinkler(final double boostThreshold, final double prefixScale, final int maxPrefixLength) {

        this.boostThreshold = boostThreshold;
        this.prefixScale = prefixScale;
        this.maxPrefixLength = maxPrefixLength;
    }

    @Override
    public String getMeasureName() {
        return "JaroWinkler";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {
        return 1.0 - similarity(x, y);
    }

    private double similarity(final String x, final String y) {

        final double jaroScore = jaro.similarity(x, y);

        if (jaroScore < boostThreshold) {
            return jaroScore;
        } else {
            int prefixLength = Math.min(Strings.commonPrefix(x, y).length(), maxPrefixLength);
            return jaroScore + (double)prefixLength * prefixScale * (1.0 - jaroScore);
        }
    }

    public String toString() {
        return "JaroWinkler [boostThreshold=" + boostThreshold + ", prefixScale=" + prefixScale + ", maxPrefixLength=" + maxPrefixLength + "]";
    }

    public static void main(String[] a) {

        new JaroWinkler().printExamples();
    }
}
