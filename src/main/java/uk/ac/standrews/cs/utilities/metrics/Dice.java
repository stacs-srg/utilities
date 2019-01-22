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

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Collection;

import static uk.ac.standrews.cs.utilities.metrics.Jaccard.intersection;

public class Dice implements NamedMetric<String> {
    @Override
    public String getMetricName() {
        return "Dice";
    }

    @Override
    public double distance(String x, String y) {
        return 1.0 - this.compare(x, y);
    }

    public double compare(String A, String B) {

        double check = CheckValues.checkNullAndEmpty(A, B);
        if (check != -1) return check;

        Collection agrams = Shingle.ngrams(topAndTail(A), 2);
        Collection bgrams = Shingle.ngrams(topAndTail(B), 2);

        if (agrams.isEmpty() && bgrams.isEmpty()) {
            return 1.0;
        } else {
            return !agrams.isEmpty() && !bgrams.isEmpty() ? 2.0 * intersection(agrams, bgrams).size() / (double) (agrams.size() + bgrams.size()) : 0.0;
        }
    }

    /**
     * Adds a character to the front and end of the string - ensures that even empty strings contain 1 2-gram.
     * @param x - a string to be encapsulated
     * @return an a string encapsulated with ^ and $
     */
    private static String topAndTail(String x) {
        return "^" + x + "$";
    }


    public static void main(String[] a) {
        Dice dice = new Dice();

        System.out.println("Dice:" );

        System.out.println("empty string/empty string: " + dice.distance("", ""));
        System.out.println("empty string/cat: " + dice.distance("", "cat"));
        System.out.println("cat/empty string: " + dice.distance("cat", ""));
        System.out.println("cat/cat: " + dice.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  dice.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + dice.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + dice.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +dice.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + dice.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + dice.distance( "n", "zoological" ) );
    }

}
