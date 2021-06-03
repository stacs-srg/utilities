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
package uk.ac.standrews.cs.utilities.phonetic;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

public class PhoneticWrapper extends StringMetric {

    private final StringEncoder encoder;
    private final Metric<String> metric;
    private final String metric_name;

    public PhoneticWrapper(StringEncoder encoder, Metric<String> metric) {

        this.encoder = encoder;
        this.metric = metric;
        this.metric_name = encoder.getClass().getSimpleName() + "-" + metric.getMetricName();
    }

    @Override
    public String getMetricName() {
        return metric_name;
    }

    @Override
    public double calculateStringDistance(String x, String y) {
        try {
            String str1 = encoder.encode(x);
            String str2 = encoder.encode(y);

            return metric.distance(str1, str2);

        } catch (EncoderException e) {
            return 1.0;
        }
    }
}
