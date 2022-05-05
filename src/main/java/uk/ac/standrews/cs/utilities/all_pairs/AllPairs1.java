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
package uk.ac.standrews.cs.utilities.all_pairs;

import uk.ac.standrews.cs.utilities.measures.implementation.FeatureVector;
import uk.ac.standrews.cs.utilities.measures.implementation.KeyFreqPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by al on 27/09/2017.
 */
public class AllPairs1 {


    private final double similarity_threshold;
    private final List<String> corpus;
    private int shingle_size = 2;


    //    MapI I1,I2,...,Im ← ∅ // from feature-index to Set of (doc, feature) pairs
    private HashMap<String,List<DocumentFeaturePair>> indexMap = new HashMap<>(); // maps from a qgram (a feature) to those docs that have that feature.
    private HashMap<String,List<SimilarityPair>> all_matches =  new HashMap<>(); // indexed by orginal string.

    private HashMap<String,Integer> max_weights = new HashMap<>();    // the maximum weights for each feature (qgram)
    private HashMap<String,FeatureVector> feature_vectors = new HashMap<>();   // the feature associated with each document in the corpus.

    private AllPairs1(double similarity, List<String> corpus ) {
        this.corpus = corpus;
        this.similarity_threshold = similarity;
        initialise( corpus );
    }

    private void initialise(List<String> corpus) {

        for( String document : corpus ) {

            FeatureVector x = new FeatureVector(document, shingle_size);
            for( KeyFreqPair feature : x.getFeatures() ) {
                Integer count = max_weights.get(feature.qgram);
                if( count == null || feature.frequency > count ) {
                    max_weights.put( feature.qgram, feature.frequency );
                }
            }
        }
    }


    public void add( String document ) {
        FeatureVector x = feature_vectors.get(document);
        // get the documents that have same features

        List<SimilarityPair> matches = find_matches(x, document);
        add_to_all_matches( matches );

        Iterator<KeyFreqPair> iter = x.getFeatureIterator();
        while (iter.hasNext()) {               //     for each i s.t. x[i] > 0 do Ii ← Ii ∪ {(x, x[i])}

            KeyFreqPair next = iter.next();

            String qgram = next.qgram;
            int count = next.frequency;
            List<DocumentFeaturePair> list = indexMap.get(qgram);
                if( list == null ) {
                list = new ArrayList();
                list.add(new DocumentFeaturePair(document,x,count));
                indexMap.put(qgram,list);
            } else {
                list.add(new DocumentFeaturePair(document,x,count)); // add_worker to existing.
            }

        }

    }

    private List<SimilarityPair> find_matches(FeatureVector x, String document) {

        HashMap<String, Double> A = new HashMap<>(); // maps from Document (should be id) to weights - empty map from vector id to weight

        List<SimilarityPair> result = new ArrayList<>(); // M←∅

        Iterator<KeyFreqPair> iter = x.getFeatureIterator();  // for each i s.t. x[i] > 0 do
        while (iter.hasNext()) {

            KeyFreqPair i = iter.next(); // all are > 0 by definition

            List<DocumentFeaturePair> candidates = indexMap.get(i.qgram);
            if( candidates != null ) {
                for (DocumentFeaturePair y : indexMap.get(i.qgram)) { // foreach(y,y[i])∈Ii do  for each document in the indexMap that exists for that feature

                    double similarity = calculate_cosine(x, y.feature_vector); // calculate cosing similarity between x and y

                    A.put(y.document, similarity);   // Add

                }
            }
        }
        for( String fv : A.keySet() ) {
            double similarity = A.get( fv );
            if( similarity > similarity_threshold ) {
                result.add( new SimilarityPair(document,fv,similarity ) );
            }
        }
        return result;
    }

    private double calculate_cosine(FeatureVector x, FeatureVector y) {
        double dot_product = 0.0d;

        for( KeyFreqPair xs : x.getFeatures() ) {
            //lookup
            dot_product += xs.frequency * y.getFrequency(xs.qgram);
        }

        return (dot_product / (magnitude(x) * magnitude(y)));
    }

    private double magnitude(FeatureVector x) {
        double result = 0.0d;
        for( KeyFreqPair xs : x.getFeatures() ) {
            result += xs.frequency;
        }
        return Math.sqrt(result);
    }

    private void add_to_all_matches(List<SimilarityPair> matches) {
        for( SimilarityPair match : matches ) {

            List<SimilarityPair> a_in_all_matches = all_matches.get( match.a );
            if( a_in_all_matches == null) {
                a_in_all_matches = new ArrayList<>();
                a_in_all_matches.add(match);
                all_matches.put( match.a, a_in_all_matches );
            } else {
                a_in_all_matches.add(match);
            }

            List<SimilarityPair> b_in_all_matches = all_matches.get( match.b );
            if( b_in_all_matches == null) {
                b_in_all_matches = new ArrayList<>();
                b_in_all_matches.add(match);
                all_matches.put( match.b, b_in_all_matches );
            } else {
                b_in_all_matches.add(match);
            }
        }

    }

    public List<SimilarityPair> getMatches( String key ) {

        return all_matches.get( key );
    }


}
