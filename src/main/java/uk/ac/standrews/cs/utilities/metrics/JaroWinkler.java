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
import com.google.common.base.Strings;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

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


public final class JaroWinkler implements NamedMetric<String> {
    private final Jaro jaro;
    private static final double PREFIX_SCALE = 0.1F;
    private static final double WINKLER_BOOST_THRESHOLD = 0.7F;
    private static final double BOOST_THRESHOLD = 0.0;
    private static final int MAX_PREFIX_LENGTH = 4;
    private final double boostThreshold;
    private final double prefixScale;
    private final int maxPrefixLength;

    public JaroWinkler() {
        this(0.0, 0.1F, 4);
    }

    public static JaroWinkler createWithBoostThreshold() {
        return new JaroWinkler(0.7F, 0.1F, 4);
    }

    public JaroWinkler(double boostThreshold, double prefixScale, int maxPrefixLength) {
        this.jaro = new Jaro();
        Preconditions.checkArgument(boostThreshold >= 0.0);
        Preconditions.checkArgument(0.0 <= prefixScale && prefixScale <= 1.0);
        Preconditions.checkArgument(maxPrefixLength >= 0);
        this.boostThreshold = boostThreshold;
        this.prefixScale = prefixScale;
        this.maxPrefixLength = maxPrefixLength;
    }

    public double distance(String a, String b) {
        return 1.0 - this.compare(a, b);
    }

    public double compare(String a, String b) {

        double check = CheckValues.checkNullAndEmpty(a, b);
        if (check != -1) return check;

        double jaroScore = this.jaro.compare(a, b);
        if (jaroScore < this.boostThreshold) {
            return jaroScore;
        } else {
            int prefixLength = Math.min(Strings.commonPrefix(a, b).length(), this.maxPrefixLength);
            return jaroScore + (double)prefixLength * this.prefixScale * (1.0 - jaroScore);
        }
    }

    public String toString() {
        return "JaroWinkler [boostThreshold=" + this.boostThreshold + ", prefixScale=" + this.prefixScale + ", maxPrefixLength=" + this.maxPrefixLength + "]";
    }

    @Override
    public String getMetricName() {
        return "JaroWinkler";
    }

    public static void main(String[] a) {
        JaroWinkler jaro = new JaroWinkler();

        System.out.println("JaroWinkler:" );

        System.out.println("empty string/empty string: " + jaro.distance("", ""));
        System.out.println("empty string/cat: " + jaro.distance("", "cat"));
        System.out.println("cat/empty string: " + jaro.distance("cat", ""));
        System.out.println("cat/cat: " + jaro.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  jaro.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + jaro.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + jaro.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +jaro.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + jaro.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + jaro.distance( "n", "zoological" ) );
    }
}
