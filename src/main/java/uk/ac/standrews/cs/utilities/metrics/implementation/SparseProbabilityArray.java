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

import java.util.Map;
import java.util.TreeMap;

public class SparseProbabilityArray {

    /*
     * once finalised, these are populated in order with probabilities that
     * average_value to ones
     */
    protected double[] finalProbs;
    protected int[] finalEvents;

    /*
     * used to build up the structure event by event, counting cardinalities
     */
    private Map<Integer, Integer> cardMap;
    private int acc;

    public SparseProbabilityArray() {

        cardMap = new TreeMap<>();
        acc = 0;
    }

    public void addEvent(int event, int card) {

        if (!cardMap.keySet().contains(event)) {
            cardMap.put(event, 0);
        }
        cardMap.put(event, cardMap.get(event) + card);
        acc += card;
    }

    public void finalise() {

        final int size = cardMap.size();
        finalEvents = new int[size];
        finalProbs = new double[size];

        int ptr = 0;
        for (int event : cardMap.keySet()) {
            finalEvents[ptr] = event;
            finalProbs[ptr++] = (double) cardMap.get(event) / acc;
        }
        cardMap = null;
    }
}
