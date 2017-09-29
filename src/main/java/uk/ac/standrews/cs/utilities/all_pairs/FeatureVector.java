package uk.ac.standrews.cs.utilities.all_pairs;

import java.util.*;

/**
 * Created by al on 27/09/2017.
 * Maintains a feature vector in frequency order.
 */
public class FeatureVector {

    private List<KeyFreqPair> state = new Vector<>();
    HashMap<String,KeyFreqPair> frequencies = new HashMap<>();

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

    Iterator<KeyFreqPair> getFeatureIterator() {
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

