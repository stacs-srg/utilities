/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class Cosine implements Metric<String> {

    public double distance( String x, String y ) {
        FeatureVector fvx = new FeatureVector(x, 2);
        FeatureVector fvy = new FeatureVector(x, 2);
        return distance( fvx, fvy );
    }

    public double distance(FeatureVector x, FeatureVector y) {
        double dot_product = 0.0d;

        for( KeyFreqPair xs : x.getFeatures() ) {
            //lookup
            dot_product += xs.frequency * y.getFrequency(xs.qgram);
        }

        return (dot_product / (magnitude(x) * magnitude(y)));
    }

    private double magnitude(FeatureVector x) {
        double result = 0.0d;
        for( KeyFreqPair xs : x.getFeatures() ) {
            result += xs.frequency;
        }
        return Math.sqrt(result);
    }

    @Override
    public String getMetricName() {
        return "Cosine";
    }

}
