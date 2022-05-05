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
package uk.ac.standrews.cs.utilities.measures;

import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.ArrayList;
import java.util.List;

/*
 *         Bag distance is a cheap method to calculate the distance between two
 *         strings. It is always smaller or equal to the edit distance, and therefore
 *         the similarity measure returned by the method is always larger than the
 *         edit distance similarity measure.
 *         For more details see for example:
 *         "String Matching with Metric Trees Using an Approximate Distance"
 *         Ilaria Bartolini, Paolo Ciaccia and Marco Patella,
 *         in Proceedings of the 9th International Symposium on String Processing
 *         and Information Retrieval, Lisbone, Portugal, September 2002.
 */
public class BagDistance extends StringMeasure {

    @Override
    public String getMeasureName() {
        return "BagDistance";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        final List<Character> list1 = toList(x);
        final List<Character> list2 = toList(y);

        for (Character ch : list1) {
            list2.remove(ch);                 // only removes if in the list
        }

        // ch must be typed as Character not char, since otherwise we call List.remove(index)...

        for (Character ch : toList(y)) {      // note make a copy of list2 because we have removed characters from original above.
            list1.remove(ch);                 // only removes if in the list
        }

        return (((double) Math.max(list1.size(), list2.size())) / Math.max(x.length(), y.length()));
    }

    private static List<Character> toList(String input) {

        final List<Character> result = new ArrayList<>();
        for (char ch : input.toCharArray()) {
            result.add(ch);
        }
        return result;
    }

    public static void main(String[] a) {

        new BagDistance().printExamples();
    }
}