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
package uk.ac.standrews.cs.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class PairTest {

    @Test
    public void intTest() {

        Pair pair = new Pair<>(0, 1);
        assertEquals(0, pair.X());
        assertEquals(1, pair.Y());
    }

    @Test
    public void stringTest() {

        Pair pair = new Pair<>("hello", "world");
        assertEquals("hello", pair.X());
        assertEquals("world", pair.Y());
    }

    @Test
    public void mixedTest() {

        Pair pair = new Pair<>("hello", 1);
        assertEquals("hello", pair.X());
        assertEquals(1, pair.Y());
    }
}
