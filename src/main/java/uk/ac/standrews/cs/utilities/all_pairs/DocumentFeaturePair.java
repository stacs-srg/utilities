package uk.ac.standrews.cs.utilities.all_pairs;

/**
 * Created by al on 27/09/2017.
 */
public class DocumentFeaturePair {

    public final String document;
    public final int count;    // of feature
    public final FeatureVector feature_vector;

    public DocumentFeaturePair(String document, FeatureVector fv, int count) {
        this.document = document;
        this.feature_vector = fv;
        this.count = count;
    }
}
