/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.metrics.implementation;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.ac.standrews.cs.utilities.metrics.implementation.SparseProbabilityArray;

import java.util.HashMap;
import java.util.Map;

public abstract class SparseProbabilityMetric extends StringMetric {

    private static final int CHAR_VAL_UPPER_BOUND = 512;
    private static final double LOG_TWO = Math.log(2);

    private static Map<String, SparseProbabilityArray> memoTable = new HashMap<>();

    protected static SparseProbabilityArray stringToSparseArray(String s) {

        if (memoTable.containsKey(s)) {
            return memoTable.get(s);

        } else {
            SparseProbabilityArray sparse_array = new SparseProbabilityArray();

            for (int i = -1; i < s.length(); i++) {
                char ch1 = 0;
                char ch2 = 0;
                try {
                    ch1 = s.charAt(i);
                    sparse_array.addEvent(ch1, 2);
                    if (ch1 > CHAR_VAL_UPPER_BOUND || ch1 == 0) {
                        throw new RuntimeException("incorrect char in SED: " + ch1 + " charval: " + ((int) (ch1)) + " from string: " + s);
                    }
                    ch2 = s.charAt(i + 1);
                } catch (IndexOutOfBoundsException e) {
                    if (i == -1) {
                        ch1 = 1;
                        ch2 = s.charAt(0);
                    } else {
                        ch2 = 1;
                    }
                }
                sparse_array.addEvent(ch1 * CHAR_VAL_UPPER_BOUND + ch2, 1);
            }
            sparse_array.finalise();
            memoTable.put(s, sparse_array);
            return sparse_array;
        }
    }

    protected static double doCalc(SparseProbabilityArray ar1, SparseProbabilityArray ar2) {

        int ar1Ptr = 0;
        int ar2Ptr = 0;
        int ar1Event = ar1.finalEvents[ar1Ptr];
        int ar2Event = ar2.finalEvents[ar2Ptr];

        boolean finished = false;
        double simAcc = 0;

        while (!finished) {

            if (ar1Event == ar2Event) {
                simAcc += hCalc(ar1.finalProbs[ar1Ptr], ar2.finalProbs[ar2Ptr]);
                ar1Ptr++;
                ar2Ptr++;
            } else if (ar1Event < ar2Event) {
                ar1Ptr++;
            } else {
                ar2Ptr++;
            }

            if (ar1Ptr == ar1.finalEvents.length) {
                ar1Event = Integer.MAX_VALUE;
            } else {

                ar1Event = ar1.finalEvents[ar1Ptr];
            }

            if (ar2Ptr == ar2.finalEvents.length) {
                ar2Event = Integer.MAX_VALUE;
            } else {
                ar2Event = ar2.finalEvents[ar2Ptr];
            }

            finished = ar1Ptr == ar1.finalEvents.length && ar2Ptr == ar2.finalEvents.length;
        }
        return (1 - (simAcc / LOG_TWO) / 2);
    }

    private static double h(double d) {
        return -d * Math.log(d);
    }

    private static double hCalc(double d1, double d2) {

        if (d1 == 0 || d2 == 0) throw new RuntimeException("illegal parameter");

        return h(d1) + h(d2) - h(d1 + d2);
    }
}
