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
package uk.ac.standrews.cs.utilities.m_tree.experiments;

import org.simmetrics.metrics.Levenshtein;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.m_tree.MTree;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class MTreeStringDictionaryCheck {

    private static MTree<String> tree;
    private static int count;

    private static String dict_file = "/usr/share/dict/words";

    public static void main(String[] args) throws Exception {


        long starttime = System.currentTimeMillis();
        tree = new MTree<>(new EditDistance());
        readin_data();

        //unixDictionarySizeTest();
        allNearestNeighbours();
        // nearestN();
        // range();
        System.out.println( "MTree time taken = " + ( System.currentTimeMillis() - starttime ) / 1000.0 );
    }

    public static void readin_data() throws Exception {

        count = 0;
        for (String line : FileManipulation.readAllLines(FileManipulation.getInputStream(Paths.get(dict_file)))) {  // file has one word per line
            tree.add(line);
            count++;
        }
    }

    private static void assertTrue(boolean assertion) {

        if (!assertion) throw new RuntimeException("assertion failed");
    }

    private static void assertEquals(Object obj1, Object obj2) {

        assertTrue(obj1.equals(obj2));
    }

    /**
     * test to ensure that the correct number of words are in MTree
     */
    private static void unixDictionarySizeTest() {
        assertEquals(count, tree.size());
    }

    /**
     * test nearest neighbour in  a dictionary of words
     */
    private static void nearestNeighbour() {

        DataDistance<String> result = tree.nearestNeighbour("absilute");
        assertEquals("absolute", result.value);
    }

    private static void allNearestNeighbours() throws IOException {
        for (String line : FileManipulation.readAllLines(FileManipulation.getInputStream(Paths.get(dict_file)))) {  // file has one word per line
            DataDistance<String> result = tree.nearestNeighbour(line);
            assertEquals(line, result.value);
        }
    }

    /**
     * test nearest N in a dictionary of words
     */
    private static void nearestN() {

        List<DataDistance<String>> results = tree.nearestN("accelerat", 5);
        List<String> values = tree.mapValues(results);

        assertTrue(values.contains("accelerate"));
        assertTrue(values.contains("accelerant"));
        assertTrue(values.contains("accelerated"));
        assertTrue(values.contains("accelerator"));
        assertTrue(values.contains("scelerat")); // noun: a villain, or extremely wicked person;
    }

    /**
     * test range search in a dictionary of words
     */
    private static void range() {

        List<DataDistance<String>> results = tree.rangeSearch("tomato", 2);
        List<String> values = tree.mapValues(results);

        assertTrue(values.contains("tomato")); // distance 0
        assertTrue(values.contains("pomato")); // distance 1
        assertTrue(values.contains("pomate")); // distance 2
        assertTrue(values.contains("potato")); // distance 2
        assertTrue(values.contains("tomcat")); // distance 2
    }

    public static class EditDistance implements NamedMetric<String> {

        Levenshtein levenshtein = new Levenshtein();

        @Override
        public double distance(String s1, String s2) {

            return levenshtein.distance(s1, s2);
        }

        @Override
        public double normalisedDistance(String s1, String s2) {

            return Metric.normalise(distance(s1, s2));
        }

        @Override
        public String getMetricName() {
            return "Levenshtein";
        }
    }
}