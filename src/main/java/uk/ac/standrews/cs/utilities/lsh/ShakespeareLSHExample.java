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
package uk.ac.standrews.cs.utilities.lsh;

import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

/**
 * Created by al on 08/09/2017.
 */
public class ShakespeareLSHExample {

    public static String FILENAME = "Shakespeare.txt";


    public static void loadupdata(MinHash mh, String filename ) throws IOException {

        File f  = new File(Resources.getResource(filename).getFile());
        String readLine = "";
        int count = 0;

        try (BufferedReader b = new BufferedReader(new FileReader(f))) {

            while ((readLine = b.readLine()) != null) {
                mh.put( strip(readLine),readLine );         // map stripped string to original (not used)
                count++;
            }
            System.out.println( "Read in " + count + " lines" );
        }
    }

    public static String strip( String src ) {
        return src.replaceAll( "\\s","" ).replaceAll("[.,~{}()!\\\\]", ""); // replace all whitespace and other punctuation stops with nothing
    }

    public static void lookupSomedata(MinHash mh, String[] sentences ) {
        for( int i = 0; i < sentences.length; i++ ) {
            System.out.println( "looking up: " + sentences[i] );
            Set<String> matches = mh.getClosest(strip(sentences[i]));

            System.out.println( "Found: size = " + matches.size() + " " + matches );
        }
    }

    private static String[] exact_matches = new String[]{
            "By holy Mary, Butts, there's knavery!",
            "For new-made honour doth forget men's names:",
            "Out, damned spot! Out, I say! One- two -why then 'tis",
            "Jul. O Romeo, Romeo! wherefore art thou Romeo?",
            "GLOUCESTER. Now is the winter of our discontent" };

    private static String[] almost_matches = new String[]{
            "** holy Mary, Butts, there's knavery!",                    // edit distance of 2 - contiguous
            "By holi Mary, Hutts, there's knavery!",                    // edit distance of 2 - non-contiguous
            "For ***-made honour doth forget men's names:",             // edit distance of 3 - contiguous
            "Out, damned BLOP! Out, I say! One- two -why then 'tis",    // edit distance of 4 - contiguous
            "Jul. O Romeo, 12345! wherefore art thou Romeo?",           // edit distance of 5 - contiguous
            "GLOUCESTER. Now is the 123456 of our discontent" };        // edit distance of 5 - contiguous

    public static void main( String[] args ) throws IOException {

        MinHash minhash = new MinHash(2,10,5);
        loadupdata( minhash,FILENAME );
//        minhash.printMap();

        lookupSomedata( minhash,exact_matches );
        lookupSomedata( minhash,almost_matches );

    }
}
