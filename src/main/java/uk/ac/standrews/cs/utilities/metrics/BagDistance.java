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


import java.util.ArrayList;
import java.util.List;

public class BagDistance {

    public BagDistance() {

    }

    public double distance(String str1, String str2) {
        return 1 - similarity(str1,str2);
    }

    public double similarity(String str1, String str2) {

        double check = CheckValues.checkNullAndEmpty(str1, str2);
        if (check != -1) return check;

        int n = str1.length();
        int m = str2.length();

        List<Integer> list1 = toList(str1);
        List<Integer> list2 = toList(str2);

        for (Integer ch : toList(str1)) {
            if( list2.contains(ch)) {
                list2.remove(ch);
            }
        }

        for (char ch : str2.toCharArray()) {
            if( list1.contains(ch)) {
                list1.remove(ch);
            }
        }

        int b = Math.max(list1.size(), list2.size());

        double w = 1.0 - (((double) b) / Math.max(n, m));


        return w;

    }

    private List<Integer> toList(String input) {
        List<Integer> result = new ArrayList<>();
        for (char ch : input.toCharArray()) {
            result.add((int) ch);
        }
        return result;
    }

    public static void main(String[] a) {
        BagDistance bd = new BagDistance();

        System.out.println("BagDistance:" );

        System.out.println("empty string/empty string: " + bd.distance("", ""));
        System.out.println("empty string/cat: " + bd.distance("", "cat"));
        System.out.println("cat/empty string: " + bd.distance("cat", ""));
        System.out.println("cat/cat: " + bd.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  bd.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + bd.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + bd.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +bd.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + bd.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + bd.distance( "n", "zoological" ) );
    }

}