package uk.ac.standrews.cs.utilities.all_pairs;

/**
 * Created by al on 27/09/2017.
 */
public class SimilarityPair {

    public final String a;
    public final String b;
    public final double similarity;

    public SimilarityPair(String a, String b, double similarity ) {
        this.a = a;
        this.b = b;
        this.similarity = similarity;
    }
}
