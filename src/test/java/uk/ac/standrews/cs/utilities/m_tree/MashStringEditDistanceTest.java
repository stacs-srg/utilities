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
package uk.ac.standrews.cs.utilities.m_tree;

import org.junit.Before;
import org.junit.Test;
import org.simmetrics.metrics.Levenshtein;
import uk.ac.standrews.cs.utilities.m_tree.experiments.euclidean.StringKeyMaker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MashStringEditDistanceTest {

    private Mash<String> tree;

    @Before
    public void setUp() throws Exception {

        tree = new Mash<>(new EditDistance(),new StringKeyMaker());
    }

    /**
     * add a single point to the tree
     */
    @Test
    public void addOne() {

        String s = "hello mum";
        tree.add(s);
        assertEquals(1, tree.size());
        assertTrue(tree.contains(s));
    }

    /**
     * add some words and find the closest
     */
    @Test
    public void checkNearest() {

        String[] words = new String[]{"girl", "boy", "fish", "flash", "shed", "crash", "hill", "moon"};

        for (String word : words) {
            tree.add(word);
        }

        // tree.showTree();

        assertEquals("crash", tree.nearestNeighbour("brash").value);
        assertEquals("boy", tree.nearestNeighbour("toy").value);
        assertEquals("hill", tree.nearestNeighbour("sill").value);
        assertEquals("hill", tree.nearestNeighbour("hole").value);
        assertEquals("moon", tree.nearestNeighbour("soon").value);
        assertEquals("fish", tree.nearestNeighbour("fist").value);
        assertEquals("shed", tree.nearestNeighbour("shod").value);
    }

    public class EditDistance implements Distance<String> {

        Levenshtein levenshtein = new Levenshtein();

        @Override
        public float distance(String s1, String s2) {

            return levenshtein.distance(s1, s2);
        }
    }
}