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

import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;
import uk.ac.standrews.cs.utilities.measures.implementation.FeatureVector;
import uk.ac.standrews.cs.utilities.measures.implementation.QgramDistribution;
import uk.ac.standrews.cs.utilities.measures.implementation.SparseDistribution;

public class Cosine extends StringMeasure {

    @Override
    public String getMeasureName() {
        return "Cosine";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    protected double calculateDistance(final String x, final String y) {

        return distance(new SparseDistribution(topAndTail(x)), new SparseDistribution(topAndTail(y)));
    }

    public double distance(final FeatureVector x, final FeatureVector y) {

        return distance(new SparseDistribution(x), new SparseDistribution(y));
    }

    private double distance(final SparseDistribution x, final SparseDistribution y) {

        x.convertToProbabilityBased();
        y.convertToProbabilityBased();

        double dot_product = 0.0d;

        for (QgramDistribution qgram : x) {

            QgramDistribution qi = y.getEntry(qgram.key);

            if (qi != null) {
                dot_product += qgram.count * qi.count;
            }
        }

        final double cosine_similarity = dot_product / (x.magnitude() * y.magnitude());
        final double angular_distance = 2.0 * Math.acos(Math.min(cosine_similarity, 1d)) / Math.PI; // Truncate at 1.0 in case of rounding error.

        if (Double.isNaN(angular_distance)) {
            throw new RuntimeException("Cosine.distance returned Nan");
        }

        return angular_distance;
    }

    public static void main(String[] args) {

        new Cosine().printExamples();
    }
}
