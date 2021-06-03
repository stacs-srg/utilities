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

import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * TODO - move this class in the tests
 *
 * Created by al on 08/09/2017.
 */
public class ShakespeareAllPairsExample {

    private static String FILENAME = "Shakespeare.txt";

    private static void loadupdata(AllPairs ap, String filename) throws IOException {

        File file = new File(Resources.getResource(filename).getFile());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            int count = 0;
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                ap.add(readLine); // map stripped string to original (not used)
                count++;
            }
            System.out.println("Read in " + count + " lines");
        }
    }

    private static void lookupSomedata(AllPairs ap, String[] sentences) {

        for (String sentence : sentences) {
            System.out.println("Looking up: " + sentence);
            List<SimilarityPair> matches = ap.getMatches(sentence);

            System.out.println("Found: size = " + matches.size() + " " + matches);
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

    public static void main(String[] args) throws IOException {

        AllPairs ap = new AllPairs(0.2f);
        loadupdata(ap, FILENAME);

        lookupSomedata(ap, exact_matches);
        lookupSomedata(ap, almost_matches);

    }
}
