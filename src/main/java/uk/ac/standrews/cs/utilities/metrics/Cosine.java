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
import uk.ac.standrews.cs.utilities.metrics.implementation.FeatureVector;
import uk.ac.standrews.cs.utilities.metrics.implementation.QgramDistribution;
import uk.ac.standrews.cs.utilities.metrics.implementation.SparseDistro;

import java.util.Iterator;

public class Cosine extends StringMetric {

    @Override
    public String getMetricName() {
        return "Cosine";
    }

    protected double calculateStringDistance(String x, String y) {

        return distance(new SparseDistro(topAndTail(x)), new SparseDistro(topAndTail(y)));
    }

    public double distance(FeatureVector x, FeatureVector y) {

        return distance(new SparseDistro(x), new SparseDistro(y));
    }

    private double distance(SparseDistro p, SparseDistro q) {

        if (p.equals(q)) { // can have same sparseDistro for differering strings: "KATARINA KRISTINA" and "KRISTINA KATARINA"
            return 0.0;
        }

        p.convertToProbabilityBased();
        q.convertToProbabilityBased();

        double dot_product = 0.0d;

        Iterator<QgramDistribution> p_iterator = p.getIterator();

        while (p_iterator.hasNext()) {

            QgramDistribution next_qgram = p_iterator.next();
            QgramDistribution qi = q.getEntry(next_qgram.key);

            if (qi != null) {
                dot_product += next_qgram.count * qi.count;
            }
        }

        double cosine_similarity = dot_product / (p.magnitude() * q.magnitude());
        double angular_distance = 2.0 * Math.acos(cosine_similarity) / Math.PI;

        if (Double.isNaN(angular_distance)) {
            throw new RuntimeException("Cosine.distance returned Nan");
        }

        return angular_distance;
    }

    public static void main(String[] args) {

        new Cosine().printExamples();
    }
}
