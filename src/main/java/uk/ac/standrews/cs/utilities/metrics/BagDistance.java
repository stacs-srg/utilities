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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 *
 *         Bag distance is a cheap method to calculate the distance between two
 *         strings. It is always smaller or equal to the edit distance, and therefore
 *         the similarity measure returned by the method is always larger than the
 *         edit distance similarity measure.
 *         For more details see for example:
 *         "String Matching with Metric Trees Using an Approximate Distance"
 *         Ilaria Bartolini, Paolo Ciaccia and Marco Patella,
 *         in Proceedings of the 9th International Symposium on String Processing
 *         and Information Retrieval, Lisbone, Purtugal, September 2002.
 */
public class BagDistance implements NamedMetric<String> {

    @Override
    public String getMetricName() {
        return "BagDistance";
    }

    public double distance(String str1, String str2) {
        return 1 - similarity(str1, str2);
    }

    @Override
    public double normalisedDistance(String a, String b) {
        return distance(a, b);
    }

    public double similarity(String str1, String str2) {

        double check = NamedMetric.checkNullAndEmpty(str1, str2);
        if (check != -1) return check;

        int n = str1.length();
        int m = str2.length();

        List<Character> list1 = toList(str1);
        List<Character> list2 = toList(str2);

        for (Character ch : list1) {
            list2.remove(ch);                 // only removes if in the list
        }

        // ch must be typed as Character not char, since otherwise we call List.remove(index)...

        for (Character ch : toList(str2)) {   // note make a copy of list2 because we have removed characters from original above.
            list1.remove(ch);                 // only removes if in the list
        }

        return 1.0 - (((double) Math.max(list1.size(), list2.size())) / Math.max(n, m));
    }

    private List<Character> toList(String input) {

        List<Character> result = new ArrayList<>();
        for (char ch : input.toCharArray()) {
            result.add(ch);
        }
        return result;
    }

    public static void main(String[] a) {

        NamedMetric.printExamples(new BagDistance());
    }
}