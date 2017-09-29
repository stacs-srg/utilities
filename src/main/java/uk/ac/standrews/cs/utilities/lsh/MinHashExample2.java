/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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

package uk.ac.standrews.cs.utilities.lsh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by al on 08/09/2017.
 */
public class MinHashExample2 {

    /**
     * A utility method used for debugging
     * @param arrai - an array to be turned into a colection
     * @return the array as a collection
     */
    private static Collection<Integer> toCollection(int[] arrai ) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for( int i = 0; i < arrai.length; i++) {
            result.add( arrai[i]);
        }
        return result;
    }

    public static final String[] ozs_words = {
            "It is becoming more and more important to find effective ways of drawing",
            "insights from {data}, originating from one or more data sources.",
            "When data originates from multiple sources, the need for identifying the",
            "same or similar entities across different data sources becomes a challenging problem.",
            "In some application domains entities might have unique identifiers attached to them",
            "which makes the identification problem trivial.",
            "However, in many application domains this is not the case.",
            "Data may be collected by distinct institutions for distinct purposes.",
            "In the worst case, distinct data sources may not have keys for uniquely identifying entities at all.",
            "More commonly, they may have an internal mechanism for identifying entities, which are not necessarily useful across databases.",
            "This is the well-established {data linkage problem}~cite{christen2012data,harron2015methodological}:",
            "the problem of identifying the same or similar entities across databases without a shared unique",
            "identifier across them.",
            "A wide range of application domains require data linkage; including the health sector, national",
            "censuses, crime and fraud detection, digital libraries, and national security.",
            " prime example of a domain that requires data linkage is that of historical demography.",
            "Vital event records are collected throughout history and geography in different forms by different institutions.",
            "These records often include birth, marriage, and death certificates.",
            "Historical demographers often need to follow an individual throughout their lifespan.",
            "This requires linking a baby on a birth certificate to a deceased person on a death certificate,",
            "the groom and the bride to their respective birth and death certificates, and so on.",
            "Pseudo-identifiers like names, gender, and date of birth are often used to identify individuals",
            "cite{christen2006comparison,wrigley1997english}.",
            "The linkage of vital event records is often done manually by demographers.",
            "Even though they often make use of software systems to store and manipulate data, they tend to decide on links after considering them individually.",
            "This is a laborious process.",
            "It is only practicable with relatively small data sets, where the subject of the study is perhaps a single town or region.",
            "For larger data sets, semi-automated or fully-automated linkage methods are desirable.",
            "This is a very active research area.",
            "Often, research groups that require data linkage to be performed on a particular data",
            "set need to adapt a technique from a catalogue of existing techniques.",
            "Several recent publications use automated linkage~cite{massey2017playing,maxwell2016state,reid2016residential}.",
            "There are two main categories of automated linkage: rule-based and probabilistic.",
            "Rule-based linkage~cite{winkler1990string, winkler1992comparative} works by engineering a",
            "problem-dependent collection of rules.",
            "Rules may be arbitrarily complex: they can use lookup tables of known names, perform different tests depending on values of fields, etc.",
            "Probabilistic linkage works by performing fuzzy string comparisons on corresponding fields.",
            "There is a body of literature on alternative string comparison algorithms to use for this purpose;",
            "these include edit-distance based methods~cite{navarro2001guided}, q-gram based methods~cite{kim2010harra,kuzu2013efficient}, Jaro and Winkler~cite{jaro1989advances,winkler1990string}, Jaccard comparison~cite{naumann2010introduction}, and others.",
            "For a survey on string comparison algorithms, see~cite{christen2012field}.",
            "String comparison algorithms are at the heart of probabilistic linkage methods.",
            "Without good string comparison algorithms, and a good understanding of the trade-offs between options,",
            "there is little hope of e};" };

    public static HashMap<String,int[]> map = new HashMap<>();

    public static void loadupdata( MinHash mh ) {

        for( int i = 0; i < ozs_words.length; i++ ) {
            String sentence = ozs_words[i];
            int[] hash = mh.createMinHashSignature( sentence, mh.DEFAULTSIGNATURESIZE, mh.DEFAULTSHINGLESIZE);
            map.put( sentence,hash );
        }
    }

    public static void showSimilarities( MinHash mh ) {

        for( int i = 0; i < ozs_words.length; i++ ) {
            String sentence = ozs_words[i];

            int[] hash = mh.createMinHashSignature( sentence, mh.DEFAULTSIGNATURESIZE, 2);
            for( String key : map.keySet() ) {

                Set<String> sentence_2grams = MinHash.ngrams(sentence,2);
                Set<String> key_2grams = MinHash.ngrams(key,2);

                int[] sentence_minHashSignature = MinHash.createMinHashSignature( sentence,MinHash.DEFAULTSIGNATURESIZE, 2 );
                int[] key_minHashSignature = MinHash.createMinHashSignature( key,MinHash.DEFAULTSIGNATURESIZE, 2 );

                System.out.println( "Sentence 1 = " + sentence );
                System.out.println( "Sentence 2 = " + key );
                Set intersection = Jaccard.intersection(sentence_2grams, key_2grams);
                System.out.println( "2grams intersection = " + intersection + " size = " + intersection.size() + ", union = " + Jaccard.union( sentence_2grams, key_2grams ).size() );
                System.out.println( "Jaccard (ngrams) = " + Jaccard.jaccard(sentence_2grams,key_2grams ) );
                System.out.println( "minhash intersection = " + Jaccard.intersection( toCollection(sentence_minHashSignature), toCollection(key_minHashSignature) ).size() + ", union = " + Jaccard.union( toCollection(sentence_minHashSignature), toCollection(key_minHashSignature) ).size() );
                System.out.println( "Jaccard (minhash) = " + Jaccard.jaccard(toCollection(sentence_minHashSignature), toCollection(key_minHashSignature)) );
                System.out.println( "----" );

            }
        }
    }


    public static void main( String[] args ) {

        MinHash mh = new MinHash();
        loadupdata(mh);
        showSimilarities(mh);
    }
}
