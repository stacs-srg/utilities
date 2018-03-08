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

import java.util.*;

/**
 * Created by al on 27/09/2017.
 * Maintains a feature vector in frequency order.
 */
public class FeatureVector {

    private List<KeyFreqPair> state = new Vector<>();
    private HashMap<String,KeyFreqPair> frequencies = new HashMap<>();

    private String document;


    public FeatureVector(String document, int shingle_size) {

        this.document = document;

        for (int i = 0; i < document.length() - shingle_size + 1; i++) {
            String next_gram = document.substring(i, i + shingle_size);
            KeyFreqPair pair = frequencies.get(next_gram);
            if (pair == null) {
                frequencies.put(next_gram, new KeyFreqPair(next_gram,1) );
            } else {
                pair.frequency++;
            }
        }

        state.addAll(frequencies.values());
        Collections.sort(state);
    }

    public List<KeyFreqPair> getFeatures() {
        return state;
    }

    public Iterator<KeyFreqPair> getFeatureIterator() {
        return state.iterator();
    }

    private int maxweight() {
        try {
            return state.get( 0 ).frequency;  // in sorted order so highest is first.
        } catch( IndexOutOfBoundsException e ) {
            return 0;
        }

    }

    public String getDocument() {
        return document;
    }

    public int getFrequency(String qgram) {
        KeyFreqPair p = frequencies.get(qgram);
        if (p == null) {
            return 0;
        } else {
            return p.frequency;
        }
    }
}

