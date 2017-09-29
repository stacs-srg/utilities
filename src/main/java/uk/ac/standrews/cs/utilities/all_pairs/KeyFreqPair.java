package uk.ac.standrews.cs.utilities.all_pairs;

/**
 * Created by al on 27/09/2017.
 */
public class KeyFreqPair implements Comparable<KeyFreqPair> {

    public String qgram;
    public int frequency;

    public KeyFreqPair( String qgram, int frequency ){
        this.qgram = qgram;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(KeyFreqPair other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}
