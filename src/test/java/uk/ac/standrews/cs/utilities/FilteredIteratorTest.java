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
package uk.ac.standrews.cs.utilities;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by graham on 02/05/2014.
 */
public class FilteredIteratorTest {

    @Test
    public void filterEvenNumbers1() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{1, 2, 3, 4, 5, 6, 7});

        assertEquals(2, evens.next().intValue());
        assertEquals(4, evens.next().intValue());
        assertEquals(6, evens.next().intValue());
        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers2() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{1, 2, 3, 4, 5, 6});

        assertEquals(2, evens.next().intValue());
        assertEquals(4, evens.next().intValue());
        assertEquals(6, evens.next().intValue());
        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers3() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{2, 3, 4, 5, 6, 7});

        assertEquals(2, evens.next().intValue());
        assertEquals(4, evens.next().intValue());
        assertEquals(6, evens.next().intValue());
        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers4() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{1, 3, 5, 7});

        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers5() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{2, 4, 6});

        assertEquals(2, evens.next().intValue());
        assertEquals(4, evens.next().intValue());
        assertEquals(6, evens.next().intValue());
        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers6() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{2});

        assertEquals(2, evens.next().intValue());
        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers7() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{3});

        assertFalse(evens.hasNext());
    }

    @Test
    public void filterEvenNumbers8() {

        Iterator<Integer> evens = makeEvensIterator(new Integer[]{});

        assertFalse(evens.hasNext());
    }

    private Iterator<Integer> makeEvensIterator(Integer[] array) {

        Iterator<Integer> original = Arrays.stream(array).iterator();
        Predicate<Integer> even_filter = t -> t % 2 == 0;
        return new FilteredIterator<>(original, even_filter);
    }
}
