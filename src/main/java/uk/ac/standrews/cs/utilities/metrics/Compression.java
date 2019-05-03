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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;

/**
 * For more information about using compression for similarity measures see:
 *                            - Cilibrasi, R. and Vitanyi, P.: Clustering by compression. IEEE Trans.
 *                            Infomat. Th. Submitted, 2004. See: http://arxiv.org/abs/cs.CV/0312044
 *                            - Keogh, E., Lonardi, S. and Ratanamahatana, C.A.: Towards parameter-free
 *                            data mining. Proceedings of the 2004 ACM SIGKDD international conference
 *                            on Knowledge discovery and data mining, pp. 206-215, Seattle, 2004.
 *                            - http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/306626
 */

// Code from Peter's Febrl system

public class Compression implements NamedMetric<String> {

    @Override
    public String getMetricName() {
        return "Compression";
    }

    public double distance(String str1, String str2) {
        return 1 - similarity(str1,str2);
    }

    public double similarity( String str1, String str2 ) {

        double check = NamedMetric.checkNullAndEmpty(str1, str2);
        if (check != -1) return check;

        try {
            double c1 = zlibcompress( str1 );
            double c2 = zlibcompress( str2 );
            double c12 = 0.5 * ( zlibcompress(str1+str2) + zlibcompress(str2+str1) );

            if (c12 == 0.0) {
                return 0.0; // Maximal distance
            }

            return 1.0 - (c12 - Math.min(c1,c2)) / Math.max(c1,c2);

        } catch (UnsupportedEncodingException e) {
            return 0.0; // // Maximal distance
        }
    }

    private double zlibcompress(String str1) throws UnsupportedEncodingException {

        Deflater compresser = new Deflater();
        byte[] input = str1.getBytes("UTF-8");
        byte[] output = new byte[100];
        compresser.setInput(input);
        compresser.finish();
        double compressedDataLength = compresser.deflate(output);
        compresser.end();
        return compressedDataLength;
    }

    public static void main(String[] a) {

        NamedMetric.printExamples(new Compression());
    }
}
