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

import com.google.common.base.Strings;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

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
public final class JaroWinkler extends StringMetric {

    private final Jaro jaro;
    private final double boostThreshold = 0.0;
    private final double prefixScale = 0.1F;
    private final int maxPrefixLength = 4;

    @Override
    public String getMetricName() {
        return "JaroWinkler";
    }

    public JaroWinkler() {

        jaro = new Jaro();
    }

    public double calculateStringDistance(String a, String b) {
        return 1.0 - similarity(a, b);
    }

    private double similarity(String a, String b) {

        double jaroScore = jaro.similarity(a, b);

        if (jaroScore < boostThreshold) {
            return jaroScore;
        } else {
            int prefixLength = Math.min(Strings.commonPrefix(a, b).length(), maxPrefixLength);
            return jaroScore + (double)prefixLength * prefixScale * (1.0 - jaroScore);
        }
    }

    public String toString() {
        return "JaroWinkler [boostThreshold=" + this.boostThreshold + ", prefixScale=" + this.prefixScale + ", maxPrefixLength=" + this.maxPrefixLength + "]";
    }

    public static void main(String[] a) {

        new JaroWinkler().printExamples();
    }
}
