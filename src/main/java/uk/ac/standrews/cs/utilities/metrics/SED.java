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

/**
 * @author Richard Connor richard.connor@strath.ac.uk
 * Implements Structural Entropic Distance
 *
 */
package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SED implements NamedMetric<String> {

    private int charValUpb;
    private Map<String, SparseProbabilityArray> memoTable;
    private static double log2 = Math.log(2);

    private static double h(double d) {
        return -d * Math.log(d);
    }

    private static double hCalc(double d1, double d2) {
        assert d1 != 0;
        assert d2 != 0;
        return h(d1) + h(d2) - h(d1 + d2);
    }

    protected static class SparseProbabilityArray {
        /*
         * used to build up the structure event by event, counting cardinalities
         */
        private Map<Integer, Integer> cardMap;
        private int acc;
        /*
         * once finalised, these are populated in order with probabilities that
         * average_value to ones
         */
        private double[] finalProbs;
        private int[] finalEvents;

        public SparseProbabilityArray() {
            this.cardMap = new TreeMap<>();
            this.acc = 0;
        }

        @SuppressWarnings("boxing")
        public void addEvent(int event, int card) {
            if (!this.cardMap.keySet().contains(event)) {
                this.cardMap.put(event, 0);
            }
            this.cardMap.put(event, this.cardMap.get(event) + card);
            this.acc += card;
        }

        @SuppressWarnings("boxing")
        public void finalise() {
            final int size = this.cardMap.size();
            this.finalEvents = new int[size];
            this.finalProbs = new double[size];

            int ptr = 0;
            for (int event : this.cardMap.keySet()) {
                this.finalEvents[ptr] = event;
                this.finalProbs[ptr++] = (double) this.cardMap.get(event)
                        / this.acc;
            }
            this.cardMap = null;
        }

        public static double SEDistance(SparseProbabilityArray ar1,
                                     SparseProbabilityArray ar2) {
            double k = doCalc( ar1,ar2 );
            return (double) Math.pow(Math.pow(2, Math.max(0,k)) - 1, 0.486);
        }

        public static double JSDistance(SparseProbabilityArray ar1,
                                       SparseProbabilityArray ar2) {
            double k = doCalc( ar1,ar2 );
            return (double) Math.sqrt(Math.max(0,k));
        }

        private static double doCalc(SparseProbabilityArray ar1,
                                     SparseProbabilityArray ar2) {
            int ar1Ptr = 0;
            int ar2Ptr = 0;
            int ar1Event = ar1.finalEvents[ar1Ptr];
            int ar2Event = ar2.finalEvents[ar2Ptr];

            boolean finished = false;
            double simAcc = 0;
            while (!finished) {
                // System.out.println(ar1Event + ";" + ar2Event);
                if (ar1Event == ar2Event) {
                    simAcc += hCalc(ar1.finalProbs[ar1Ptr],
                            ar2.finalProbs[ar2Ptr]);
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
                finished = ar1Ptr == ar1.finalEvents.length
                        && ar2Ptr == ar2.finalEvents.length;
            }
            double k = (1 - (simAcc / log2) / 2);
            return k;
        }
    }

    /**
     * @param maxCharVal
     */
    public SED(int maxCharVal) {
        if (maxCharVal > Math.sqrt(Integer.MAX_VALUE)) {
            throw new RuntimeException("char val too large for SED");
        }
        this.charValUpb = maxCharVal + 1;
        this.memoTable = new HashMap<>();
    }

    protected SparseProbabilityArray stringToSparseArray(String s) {
        if (this.memoTable.containsKey(s)) {
            return this.memoTable.get(s);
        } else {
            SparseProbabilityArray spa = new SparseProbabilityArray();

            for (int i = -1; i < s.length(); i++) {
                char ch1 = 0;
                char ch2 = 0;
                try {
                    ch1 = s.charAt(i);
                    spa.addEvent(ch1, 2);
                    if (ch1 > this.charValUpb || ch1 == 0) {
                        throw new RuntimeException("incorrect char val in SED");
                    }
                    ch2 = s.charAt(i + 1);
                } catch (IndexOutOfBoundsException e) {
                    if (i == -1) {
                        ch1 = 1;
                        ch2 = s.charAt(0);
                    } else {
                        // ch1 = s.charAt(i); //redundant
                        ch2 = 1;
                    }
                }
                spa.addEvent(ch1 * this.charValUpb + ch2, 1);
            }
            spa.finalise();
            this.memoTable.put(s, spa);
            return spa;
        }
    }

    @Override
    public double distance(String x, String y) {
        if( x == null || x.equals( "" ) ) {
            if( y == null || y.equals( "" ) ) {
                return 0;
            } else {
                return 1;
            }
        }
        if( y == null || y.equals( "" ) ) {
            return 1;
        }
        if( x.equals( y ) ) {
            return 0;
        }
       //
        SparseProbabilityArray s1 = stringToSparseArray(x);
        SparseProbabilityArray s2 = stringToSparseArray(y);
        return SparseProbabilityArray.SEDistance(s1, s2);
    }

    @Override
    public String getMetricName() {
        return "sed";
    }

    public static void main(String[] a) {
        SED sed = new SED(255);

        System.out.println("SED:" );


        System.out.println("null/cat: " + sed.distance(null, "cat"));
        System.out.println("cat/null: " + sed.distance("cat", null));
        System.out.println("null/null: " + sed.distance(null, null));
        System.out.println("empty string/empty string: " + sed.distance("", ""));
        System.out.println("empty string/cat: " + sed.distance("", "cat"));
        System.out.println("cat/empty string: " + sed.distance("cat", ""));
        System.out.println("cat/cat: " + sed.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  sed.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + sed.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + sed.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +sed.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + sed.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + sed.distance( "n", "zoological" ) );
    }

}